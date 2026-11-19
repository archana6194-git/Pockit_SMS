const V='pockit-v7',CORE=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(V).then(c=>c.addAll(CORE)).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==V).map(x=>caches.delete(x)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{const q=e.request;if(q.method!=='GET')return;
const same=new URL(q.url).origin===location.origin,put=n=>{if(n&&n.ok){const c=n.clone();caches.open(V).then(x=>x.put(q,c))}return n};
e.respondWith(same?fetch(q).then(put).catch(()=>caches.match(q).then(r=>r||caches.match('./index.html'))):caches.match(q).then(r=>r||fetch(q).then(put)))});
