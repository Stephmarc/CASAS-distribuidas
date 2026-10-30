import {PGlite} from '@electric-sql/pglite';
import fs from 'node:fs';
import {fileURLToPath} from 'node:url';
const root=fileURLToPath(new URL('../',import.meta.url));
let checks=0;
for(const svc of fs.readdirSync(root+'services')){
 const pg=new PGlite();const dir=root+'services/'+svc+'/src/main/resources/db/migration/';
 const files=fs.readdirSync(dir).filter(x=>x.endsWith('.sql')).sort((a,b)=>parseInt(a.slice(1))-parseInt(b.slice(1)));
 const upgradeIds=[crypto.randomUUID(),crypto.randomUUID()];
 for(const f of files){
  if(f.startsWith('V13__')){
   await pg.query("insert into event_outbox(event_id,aggregate_id,payload,created_at,attempts) values($1,'upgrade','{}','2026-10-16T02:00:00Z',0)",[upgradeIds[1]]);
   await pg.query("insert into event_outbox(event_id,aggregate_id,payload,created_at,attempts) values($1,'upgrade','{}','2026-10-16T01:00:00Z',0)",[upgradeIds[0]]);
  }
  await pg.exec(fs.readFileSync(dir+f,'utf8'));checks++;
 }
 const upgrade=await pg.query("select event_id from event_outbox where aggregate_id='upgrade' order by dispatch_seq");
 if(upgrade.rows[0].event_id!==upgradeIds[0])throw new Error('Upgrade did not preserve chronological pending order '+svc);checks++;
 await pg.exec("delete from event_outbox where aggregate_id='upgrade'");
 const sample=[crypto.randomUUID(),crypto.randomUUID(),JSON.stringify({type:'CHECK'})];
 await pg.query('insert into event_outbox(event_id,aggregate_id,payload,created_at,attempts) values($1,$2,$3,now(),0)',sample);
 await pg.query('insert into event_outbox(event_id,aggregate_id,payload,created_at,attempts) values($1,$2,$3,now(),0)',[crypto.randomUUID(),sample[1],sample[2]]);
 const sql="select o.event_id,o.aggregate_id,o.payload,o.attempts from event_outbox o where o.published_at is null and (o.next_attempt_at is null or o.next_attempt_at<=now()) and not exists(select 1 from event_outbox prior where prior.aggregate_id=o.aggregate_id and prior.published_at is null and prior.dispatch_seq<o.dispatch_seq) order by o.dispatch_seq limit 1 for update of o skip locked";
 const rows=(await pg.query(sql)).rows;if(rows.length!==1||rows[0].event_id!==sample[0])throw new Error('Outbox order invalid '+svc);checks++;
 await pg.query("update event_outbox set next_attempt_at=now()+interval '1 hour' where event_id=$1",[sample[0]]);
 if((await pg.query(sql)).rows.length!==0)throw new Error('A later event bypassed its pending predecessor '+svc);checks++;
 await pg.query('update event_outbox set published_at=now() where event_id=$1',[sample[0]]);
 if((await pg.query(sql)).rows.length!==1)throw new Error('Next event not released '+svc);checks++;
 async function uniqueViolation(query){try{await pg.exec(query)}catch(e){if(e.code==='23505'){checks++;return;}throw e;}throw new Error('Expected uniqueness conflict '+svc);}
 if(svc==='visita-ms'){
  const insert="insert into visitas(cliente_id,propiedad_id,agente_id,fecha_hora,estado,created_at,updated_at) values(1,1,1,'2027-01-01T10:00:00Z','SOLICITADA',now(),now())";
  await pg.exec(insert);await uniqueViolation(insert);await pg.exec("update visitas set estado='CANCELADA'");await pg.exec(insert);checks++;
 }
 if(svc==='reserva-ms'){
  const insert="insert into reservas(cliente_id,propiedad_id,fecha_expiracion,monto_reserva,estado,created_at,updated_at) values(1,1,now()+interval '1 hour',500,'PENDIENTE',now(),now())";
  await pg.exec(insert);await uniqueViolation(insert);await pg.exec("update reservas set estado='EXPIRADA'");await pg.exec(insert);checks++;
 }
 if(svc==='pago-ms'){
  await pg.exec("insert into pagos(orden_id,monto,moneda,proveedor,idempotency_key,estado,created_at,updated_at) values(1,500,'PEN','SANDBOX_LOCAL','first','PENDIENTE',now(),now())");
  await uniqueViolation("insert into pagos(orden_id,monto,moneda,proveedor,idempotency_key,estado,created_at,updated_at) values(1,500,'PEN','SANDBOX_LOCAL','second','APROBADO',now(),now())");
 }
 console.log('PASS migrations and aggregate queue '+svc);
 await pg.close();
}
console.log('SQL checks passed:',checks);
