const {chromium}=require('/tmp/claude-0/-home-user-PodC/3b225bc1-29f5-565a-b3cd-63f757f33bda/scratchpad/lib/node_modules/playwright-core');const {spawn}=require('child_process');const fs=require('fs');
(async()=>{const {tl,T}=JSON.parse(fs.readFileSync('timeline.json'));const FPS=30,N=Math.ceil(T*FPS);
const b=await chromium.launch({executablePath:'/opt/pw-browsers/chromium-1194/chrome-linux/chrome'});const p=await b.newPage({viewport:{width:2600,height:1500}});
await p.addInitScript(()=>{window.requestAnimationFrame=()=>0});
await p.addInitScript(()=>{let z=1234567;Math.random=()=>{z=(z*16807)%2147483647;return z/2147483647}});
await p.goto('file://'+process.argv[2]);await p.waitForTimeout(1000);await p.evaluate(()=>FONTS);
// simplex-noise (MIT, github.com/jwagner/simplex-noise.js) for organic camera drift, wind and particles
const sn=fs.readFileSync('/tmp/claude-0/-home-user-PodC/3b225bc1-29f5-565a-b3cd-63f757f33bda/scratchpad/lib/node_modules/simplex-noise/dist/cjs/simplex-noise.js','utf8');
await p.evaluate(src=>{const module={exports:{}};const exports=module.exports;new Function('module','exports',src)(module,exports);window.SN=module.exports},sn);
await p.addScriptTag({content:fs.readFileSync(__dirname+'/beats.js','utf8')});
await p.evaluate(w=>{window.WORDS=w},fs.existsSync('words.json')?JSON.parse(fs.readFileSync('words.json')):{});
await p.evaluate(()=>{cv.width=2560;cv.height=1440;window.sh=function(on=true,d=5){c.shadowColor=on?'rgba(0,0,0,.28)':'transparent';c.shadowBlur=on?d*3.2:0;c.shadowOffsetY=on?d*2:0;c.shadowOffsetX=0};grainPat=null});
await p.evaluate(tl=>{window.TL=tl;const starts={};tl.forEach(([s,l,a])=>{if(starts[s]===undefined)starts[s]=a-0.9});starts[0]=0;window.ST=starts;
 let seed=7;const rnd=()=>{seed=(seed*16807)%2147483647;return seed/2147483647};
 const n2=SN.createNoise2D(rnd),n3=SN.createNoise3D(rnd);
 const XF=1.6; // crossfade seconds
 const ss=x=>x<=0?0:x>=1?1:x*x*x*(x*(x*6-15)+10); // smootherstep
 const off=document.createElement('canvas');off.width=W*2;off.height=H*2;const oc=off.getContext('2d');
 const DAY=new Set([]),NIGHT=new Set([0]);
 const motes=[...Array(40)].map((_,i)=>({x:rnd()*W,y:rnd()*H,r:1.5+rnd()*2.5,k:i}));
 function cam(s,lt,dur){ // continuous Ken Burns push-in + handheld drift
  const p=Math.min(1,lt/dur),z=1.05+0.07*ss(p)+0.012*n2(s*9,lt*.15);
  const dx=n2(s*3+1,lt*.12)*14+(s%2?-1:1)*30*(p-.5),dy=n2(s*3+2,lt*.12)*8,rot=n2(s*3+3,lt*.08)*.006;
  c.translate(W/2+dx,H/2+dy);c.rotate(rot);c.scale(z,z);c.translate(-W/2,-H/2)}
 function sceneDur(s){const ls=TL.filter(r=>r[0]===s);return ls[ls.length-1][3]-ST[s]+1.5}
 function drawScene(s,l,gt){const lt=gt-ST[s];c.save();cam(s,lt,sceneDur(s));S[s].draw(lt+4,l);ambient(s,lt);c.restore()}
 function ambient(s,t){
  if(DAY.has(s)){ // drifting paper clouds and flapping paper birds
   if(s!==2)for(let i=0;i<3;i++){const x=((i*480+t*(14+i*6))%(W+400))-200,y=90+i*55+n2(i,t*.1)*8;fill('rgba(255,255,255,.92)',()=>{ell(x,y,70,26);ell(x+45,y-14,44,26);ell(x-40,y-8,38,20)},4)}
   for(let i=0;i<4;i++){const ph=(t*.07+i*.27)%1,x=-80+ph*(W+160),y=150+i*28+Math.sin(ph*9+i)*20,f=Math.sin(t*10+i*2)*14;
    c.save();c.strokeStyle='#3a3226';c.lineWidth=3;c.lineCap='round';c.beginPath();c.moveTo(x-16,y-f);c.quadraticCurveTo(x-6,y-8,x,y);c.quadraticCurveTo(x+6,y-8,x+16,y-f);c.stroke();c.restore()}}
  // floating motes (fireflies at night, dust by day)
  c.save();c.globalCompositeOperation=NIGHT.has(s)?'lighter':'source-over';
  motes.forEach(m=>{const x=(m.x+n3(m.k,0,t*.08)*160+t*8)%W,y=(m.y+n3(m.k,9,t*.08)*90-t*6+H)%H,a=NIGHT.has(s)?.35+.35*Math.sin(t*2+m.k):.25;
   c.fillStyle=NIGHT.has(s)?`rgba(255,220,120,${a})`:`rgba(255,250,235,${a})`;c.beginPath();c.arc(x,y,m.r*(NIGHT.has(s)?1.4:1),0,7);c.fill()});c.restore()}
 function wrap(text,maxW){const w=text.split(' ');const out=[];let cur='';for(const x of w){const t=cur?cur+' '+x:x;if(c.measureText(t).width>maxW&&cur){out.push(cur);cur=x}else cur=t}out.push(cur);return out}
 const FILW=["Miguel López de Legazpi", "Rajah Humabon", "Lola Bining", "Ati-Atihan", "Santo Niño", "San Isidro", "leche flan", "kare-kare", "bayanihan", "babaylan", "Cebuanos", "Lapulapu", "MassKara", "barangay", "Sinulog", "Pahiyas", "Bacolod", "Mabuhay", "carabao", "fiestas", "Bining", "barrio", "fiesta", "diwata", "Mactan", "Kalibo", "Lucban", "Quezon", "kiping", "Lechon", "pancit", "Manila", "Tikoy", "anito", "Aklan", "anak", "Cebu", "Ati", "ang"];const FRX=new RegExp('\\b('+FILW.map(w=>w.replace(/[-]/g,'\\-')).join('|')+')\\b','gi');
 function hiToks(t){const hl=new Array(t.length).fill(false);let m;FRX.lastIndex=0;
  while((m=FRX.exec(t))){let e=m.index+m[0].length;const suf=t.slice(e).match(/^[’']s/);if(suf)e+=2;for(let i=m.index;i<e;i++)hl[i]=true}
  const out=[];const re=/\S+/g;let w;while((w=re.exec(t))){const segs=[];for(let i=w.index;i<w.index+w[0].length;i++){const h=hl[i];if(segs.length&&segs[segs.length-1].h===h)segs[segs.length-1].s+=t[i];else segs.push({s:t[i],h})}
   out.push({w:w[0],segs})}return out}
 window.R=(gt)=>{let cs=0,cl=0;for(const [s,l,a] of TL){if(gt>=ST[s]){if(s!==cs){cs=s;cl=0}if(gt>=a)cl=l}}
  {const r=TL.find(x=>x[0]===cs&&x[1]===cl);window.CL={s:cs,l:cl,lt:r?gt-r[2]:-1}}
  c.setTransform(2,0,0,2,0,0);const tr=(gt-ST[cs])/XF;
  if(cs>0&&tr<1){drawScene(cs-1,S[cs-1].lines.length-1,gt);oc.clearRect(0,0,W,H);oc.drawImage(cv,0,0);
   drawScene(cs,cl,gt);c.save();c.globalAlpha=1-ss(tr);c.drawImage(off,0,0,W,H);c.restore()}
  else drawScene(cs,cl,gt);
  if(!grainPat)grainPat=c.createPattern(grain,'repeat');c.fillStyle=grainPat;c.fillRect(0,0,W,H);
  const v=c.createRadialGradient(W/2,H/2,H*.4,W/2,H/2,H*.95);v.addColorStop(0,'rgba(0,0,0,0)');v.addColorStop(1,'rgba(0,0,0,.28)');c.fillStyle=v;c.fillRect(0,0,W,H);
  // framed stage: shrink the scene so subtitles get their own strip below it
  const SX=112,SY=14,SW=1056,SHh=594;oc.clearRect(0,0,W,H);oc.drawImage(cv,0,0);
  c.setTransform(2,0,0,2,0,0);c.fillStyle='#24302a';c.fillRect(0,0,W,H);c.fillStyle=grainPat;c.fillRect(0,0,W,H);
  c.save();sh(true,8);c.fillStyle='#000';c.fillRect(SX,SY,SW,SHh);sh(false);c.drawImage(off,SX,SY,SW,SHh);c.restore();
  // chapter label glides in (top-right corner of the stage)
  const ci=ss(Math.min(1,(gt-ST[cs]-.4)/.8));c.save();c.globalAlpha=ci;c.font='700 20px Fredoka,sans-serif';const cw=c.measureText(S[cs].chap).width+30;
  c.translate(SX+SW-cw-18+(1-ci)*40,SY+16);c.rotate(.03);sh(true,3);c.fillStyle='#e8743b';c.fillRect(0,0,cw,34);sh(false);c.fillStyle='#fff';c.fillText(S[cs].chap,15,24);c.restore();
  // subtitles on their own strip, fading in and out
  for(const [s,l,a,e] of TL){const k=Math.min(ss((gt-a+.25)/.35),ss((e+.6-gt)/.35));if(k<=0)continue;
   c.save();c.globalAlpha=k;c.font='700 25px Nunito,sans-serif';const toks=hiToks(S[s].lines[l]),rows=[[]];let rw=0;const spw=c.measureText(' ').width;
   for(const tk of toks){const w=c.measureText(tk.w).width;if(rw+w>1150&&rows[rows.length-1].length){rows.push([]);rw=0}rows[rows.length-1].push({...tk,wd:w});rw+=w+spw}
   const top=SY+SHh,lh=31,y0=top+(H-top-rows.length*lh)/2+23+(1-k)*6;c.textAlign='left';
   rows.forEach((r,i)=>{const tw=r.reduce((a,b)=>a+b.wd,0)+spw*(r.length-1);let x=W/2-tw/2;r.forEach(tk=>{let xx=x;tk.segs.forEach(sg=>{c.fillStyle='#f3ecd6';c.fillText(sg.s,xx,y0+i*lh);xx+=c.measureText(sg.s).width});x+=tk.wd+spw})});c.restore()}
  return cv.toDataURL('image/jpeg',.93)}},tl);
const F0=process.argv[5]!==undefined?+process.argv[5]:null,F1=process.argv[6]!==undefined?+process.argv[6]:null;
const ff=F0!==null?spawn('ffmpeg',['-y','-loglevel','error','-f','image2pipe','-framerate',''+FPS,'-i','-','-c:v','libx264','-preset','medium','-crf','16','-tune','animation','-pix_fmt','yuv420p',process.argv[4]],{stdio:['pipe','inherit','inherit']}):spawn('ffmpeg',['-y','-loglevel','error','-f','image2pipe','-framerate',''+FPS,'-i','-','-i','mix.wav','-c:v','libx264','-preset','medium','-crf','19','-tune','animation','-pix_fmt','yuv420p','-c:a','aac','-b:a','160k','-shortest','-movflags','+faststart',process.argv[4]||'story-1440p-raw.mp4'],{stdio:['pipe','inherit','inherit']});
const one=process.argv[3];if(one){for(const t of one.split(',')){const d=await p.evaluate(t=>R(t),+t);fs.writeFileSync('p'+t+'.jpg',Buffer.from(d.split(',')[1],'base64'))}ff.kill();await b.close();return}
for(let i=(F0!==null?F0:0);i<(F1!==null?Math.min(F1,N):N);i++){const d=await p.evaluate(t=>R(t),i/FPS);const buf=Buffer.from(d.split(',')[1],'base64');if(!ff.stdin.write(buf))await new Promise(r=>ff.stdin.once('drain',r));if(i%2000===0)console.log(i,'/',N)}
ff.stdin.end();await new Promise(r=>ff.on('close',r));await b.close();console.log('done')})();
