find . -type f -name '*.gradle' -print0 | xargs -0 sed -i 's/jcenter()/mavenCentral()/g'
find . -type f -name '*.gradle' -print0 | xargs -0 sed -i "s|maven { url 'https://jcenter\.bintray\.com' }|mavenCentral()|g"
