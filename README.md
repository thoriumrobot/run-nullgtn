Performance Benchmarking of Java Null Safety Tools
------------
Evaluates build times for Java projects using [NullAway](https://github.com/uber/NullAway), [Checker Framework](https://github.com/typetools/checker-framework)'s [Nullness Checker](https://checkerframework.org/manual/#nullness-checker), and [Eradicate](https://fbinfer.com/docs/eradicate.html).
  - [Setup](#setup)
  - [Usage](#usage)
  - [Helper scripts](#helper-scripts)
  - [Troubleshooting](#troubleshooting)
___

### Setup ###
  * Install [JDK 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html), [Python 3](https://docs.python.org/3/using/index.html), [pip](https://pip.pypa.io/en/stable/installing/)
  * Install [Checker Framework](https://checkerframework.org/manual/#installation) and [Infer](https://fbinfer.com/docs/getting-started.html)
  * Optional for Android projects: Install [Android SDK Tools](https://developer.android.com/studio/#downloads) and use [sdkmanager](https://developer.android.com/studio/command-line/sdkmanager) to install required packages
  * `mkdir <working_dir> && cd <working_dir>`
  * `https://github.com/thoriumrobot/run-nullgtn`
  * `git clone -b sb_compile-bench https://github.com/subarnob/NullAway`
  * `cd nullaway-eval`
  * `pip install -r requirements.txt`
  * Update paths in `config.ini`
___

### Usage ###
```

Generate project log:

./mklog.sh <project> <original|modified>

---

python eval_repos.py [options] [tools] [projects]

Options:
    -w  or -warn              : Log all build warnings; only log first warning by default.
    -d  or -daemon            : Build with warmed up JIT cache on a daemonized compiler.
Tools:
    -ba or -base              : Disable Nullness Checking
    -nu or -nullaway          : NullAway
    -ch or -checkerframework  : Checker Framework's Nullness Checker
    -er or -eradicate         : Infer:Eradicate
  * All tools are used by default.

Projects: project names OR relative paths to root directories.
  * All projects listed in eval_repos.txt are evaluated by default.
```
#### Example ####
```
python eval_repos.py -daemon -nullaway -checkerframework okbuck jib
```
This builds 2 projects- `okbuck` and `jib`, using the 2 tools- NullAway and Checker Framework, with a daemonized compiler.
___

### Helper scripts ###
1. **Plotting build times**:
```
python plot.py [-n|-normalize] [input_file]
```
Expects `input_file` in [CSV](https://en.wikipedia.org/wiki/Comma-separated_values) format, loads `result.csv` by default.  
Generates `<input_file_name>_abs.html` for absolute build times, or `<input_file_name>_norm.html` for normalized build times if `-normalize` option is set.

2. **Summarizing build warnings**:
```
python warns.py <log_file>
```
Generates `warns.txt`.

3. **Finding annotated projects**:  
Searches [GitHub](https://github.com/) for [Gradle](https://gradle.org/) projects that use [NullAway](https://github.com/uber/NullAway). Requires [GitHub API token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) in `~/.ghtoken`.
```
python list_repos.py
```
___

### Troubleshooting ###
If all builds of a project fail, the Gradle build cache is likely invalidated.  
To fix this, re-capture the Gradle compile tasks' arguments:
```
python javac_args.py [projects]
```

---

Detailed setup guide:

wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh
chmod +x Miniconda3-latest-Linux-x86_64.sh
./Miniconda3-latest-Linux-x86_64.sh
rm Miniconda3-latest-Linux-x86_64.sh

mkdir naenv
cd naenv

conda create -n naenv openjdk=8 python=3.11 pip maven gradle -c anaconda -c conda-forge
conda activate naenv

wget https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip
wget https://checkerframework.org/checker-framework-3.42.0.zip
wget https://github.com/facebook/infer/releases/download/v1.1.0/infer-linux64-v1.1.0.tar.xz

sudo apt-get install unzip

unzip sdk-tools-linux-4333796.zip -d ./android-sdk
unzip checker-framework-3.42.0.zip
tar -xf infer-linux64-v1.1.0.tar.xz

rm *.zip
rm *.xz

git clone https://github.com/thoriumrobot/run-nullgtn
git clone -b sb_compile-bench https://github.com/subarnob/NullAway

cd nullaway-eval
pip install -r requirements.txt

<Update paths in config.ini>

conda env config vars set ANDROID_HOME=/home/ubuntu/naenv/android-sdk
conda env config vars set PATH=/home/ubuntu/naenv/checker-framework-3.42.0/checker/bin:/home/ubuntu/naenv/infer-linux64-v1.1.0/bin/:/home/ubuntu/naenv/android-sdk/tools/bin/:$PATH
conda env config vars set CHECKERFRAMEWORK=/home/ubuntu/naenv/checker-framework-3.42.0
conda activate naenv

sdkmanager --licenses

python eval_repos.py -w -nu
rm eval.log

./mklog.sh AutoDispose original
./mklog.sh AutoDispose qwen

./mklog.sh butterknife original
./mklog.sh butterknife qwen

./mklog.sh ColdSnap original
./mklog.sh ColdSnap qwen

./mklog.sh jib original
./mklog.sh jib qwen

./mklog.sh meal-planner original
./mklog.sh meal-planner qwen

./mklog.sh picasso original
./mklog.sh picasso qwen

./mklog.sh QRContact original
./mklog.sh QRContact qwen

./mklog.sh skaffold-tools-for-java original
./mklog.sh skaffold-tools-for-java qwen

./mklog.sh uLeak original
./mklog.sh uLeak qwen


