import fs from 'node:fs';import path from 'node:path';import {fileURLToPath} from 'node:url';import {Kafka,logLevel} from 'kafkajs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const gateway=process.env.CASA_GATEWAY||'http://localhost:28080';const kc=process.env.CASA_KEYCLOAK||'http://localhost:28090';
const kafka=new Kafka({clientId:'casas-replay-check',brokers:[process.env.KAFKA_BOOTSTRAP_SERVERS||'localhost:29092'],logLevel:logLevel.NOTHING});
const consumer=kafka.consumer({groupId:'casas-verificacion-'+Date.now()});const producer=kafka.producer();
async function main(){
 const tokenResponse=await fetch(kc+'/realms/casa-o-nada/protocol/openid-connect/token',{method:'POST',body:new URLSearchParams({client_id:'casa-o-nada-tests',grant_type:'password',username:'admin',password:'Admin123!'})});if(!tokenResponse.ok)throw Error('No se pudo autenticar ADMIN');const token=(await tokenResponse.json()).access_token;
 const headers={Authorization:'Bearer '+token};async function contracts(){const r=await fetch(gateway+'/api/v1/contratos',{headers});if(!r.ok)throw Error('Contratos HTTP '+r.status);return r.json()}
 const before=await contracts();let resolveEvent;const eventPromise=new Promise(r=>resolveEvent=r);let selected;
 await consumer.connect();await consumer.subscribe({topic:'casa.operaciones.v1',fromBeginning:true});
 await consumer.run({eachMessage:async({message})=>{let e;try{e=JSON.parse(message.value.toString())}catch{return}if(!selected&&e.type==='CREAR_CONTRATO'&&before.some(c=>c.ordenId===e.ordenId)){selected=e;resolveEvent(e)}}});
 const timer=setTimeout(()=>resolveEvent(null),30000);const event=await eventPromise;clearTimeout(timer);await consumer.disconnect();if(!event)throw Error('Primero ejecute test-stability.ps1 para crear un contrato confirmado.');
 const countBefore=before.filter(c=>c.ordenId===event.ordenId).length;if(countBefore!==1)throw Error('El caso inicial debe tener un contrato');await producer.connect();await producer.send({topic:'casa.operaciones.v1',messages:[{key:event.sagaId,value:JSON.stringify(event)}]});await producer.disconnect();
 await new Promise(r=>setTimeout(r,7000));const after=(await contracts()).filter(c=>c.ordenId===event.ordenId).length;
 const logs=path.join(root,'.run/logs/casa-contrato-ms.log');const duplicate=fs.existsSync(logs)&&fs.readFileSync(logs,'utf8').split('\n').some(l=>l.includes(event.eventId)&&l.includes('duplicate=true'));
 const result={fecha:new Date().toISOString(),eventId:event.eventId,sagaId:event.sagaId,ordenId:event.ordenId,contratosAntes:countBefore,contratosDespues:after,duplicadoConfirmadoEnConsumidor:duplicate,resultado:after===1&&duplicate?'PASS':'FAIL'};
 fs.mkdirSync(root+'/.run',{recursive:true});fs.writeFileSync(root+'/.run/replay-results.json',JSON.stringify(result,null,2));console.log(result);if(result.resultado!=='PASS')throw Error('No se pudo confirmar el descarte del duplicado en el consumidor');
}
main().catch(async e=>{console.error(e.message);await consumer.disconnect().catch(()=>{});await producer.disconnect().catch(()=>{});process.exitCode=1});
