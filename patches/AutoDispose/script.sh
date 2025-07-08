# Apply the original NullAway version patch
git apply /home/thoriumrobot/project/naenv/nullaway

# Replace jcenter() with mavenCentral() in build.gradle
sed -i 's/jcenter()/mavenCentral()/g' build.gradle

# Replace jcenter() with mavenCentral() in settings.gradle  
sed -i 's/jcenter()/mavenCentral()/g' settings.gradle 