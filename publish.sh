 git checkout --orphan gh-pages
 lein cljsbuild once min
 cp resources/public/index.html .
 cp -r resources/public/js/ .
 git add index.html js/compiled/app.js
 git commit -m "Creating github page"
 git push --set-upstream origin gh-pages
 git checkout master
