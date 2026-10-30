import {spawnSync} from 'node:child_process';
import fs from 'node:fs';import path from 'node:path';import {createRequire} from 'node:module';import * as esbuild from 'esbuild-wasm';
const require=createRequire(import.meta.url);const root=path.dirname(new URL(import.meta.url).pathname.replace(/^\/(.:)/,'$1'));
process.chdir(decodeURIComponent(root));
const result=spawnSync(process.execPath,['node_modules/@angular/compiler-cli/bundles/src/bin/ngc.js','-p','tsconfig.json'],{stdio:'inherit'});if(result.status!==0)process.exit(result.status||1);
fs.mkdirSync('dist',{recursive:true});
await esbuild.build({entryPoints:['compiled/main.js'],bundle:true,format:'esm',target:'es2022',absWorkingDir:process.cwd(),outfile:path.resolve('dist/main.js'),minify:true,define:{ngDevMode:'false',ngJitMode:'true'},plugins:[{name:'explicit-resolution',setup(build){build.onResolve({filter:/.*/},args=>{if(args.kind==='entry-point')return {path:path.resolve(args.path)};if(args.kind==='entry-point')return {path:path.resolve(args.path)};if(args.path.startsWith('node:'))return {path:args.path,external:true};try{return {path:args.path.startsWith('.')?require.resolve(path.resolve(path.dirname(args.importer),args.path)):require.resolve(args.path,{paths:[process.cwd(),path.dirname(args.importer)]})}}catch{return undefined}})}}]});
for(const f of ['index.html','styles.css'])fs.copyFileSync(f,'dist/'+f);if(fs.existsSync('public'))fs.cpSync('public','dist',{recursive:true});console.log('CASAS frontend construido en dist');
