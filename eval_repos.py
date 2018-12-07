# mvn install:install-file -Dfile=~/workspace/uber/NullAway/nullaway/build/libs/nullaway-0.6.4-SNAPSHOT.jar -DgroupId=com.uber.nullaway -DartifactId=nullaway -Dversion=0.6.4-SNAPSHOT -Dpackaging=jar

import os.path, subprocess, logging, re, argparse

repo_prefix = "repos"
log_file = "eval.log"
stats_file = "result.csv"

repo_list = "eval_repos.txt"
patch_prefix = "patches"
script_file = "script.sh"

os.environ["ANDROID_HOME"] = "/usr/lib/android-sdk"
os.environ["CHECKERFRAMEWORK"] = "/usr/lib/checker-framework-2.5.8"
if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	parser.add_argument('-p','--purge',action='store_true',default=False,help='purge downloaded repositories')
	args = parser.parse_args()
	log = logging.getLogger("log")
	log.setLevel(logging.DEBUG)
	hdlr = logging.FileHandler(log_file)
	hdlr.setFormatter(logging.Formatter('%(asctime)s| %(levelname)s: %(message)s'))
	log.addHandler(hdlr)

def repo_name(repo_url): return repo_url.rpartition('/')[2]
def repo_dir(repo_url): return repo_prefix+"/"+repo_name(repo_url)

patches = ["base","nullaway","checkerframework"]
def patch_file(repo_url, patch_type, check=True):
	if patch_type < len(patches):
		patch_path = patch_prefix+"/"+repo_name(repo_url)+"/"+patches[patch_type]
		if not check: return patch_path
		if os.path.exists(patch_path):
			return os.path.abspath(patch_path)
	return None

def repo_hash(repo_url):
	hash_file = patch_prefix+"/"+repo_name(repo_url)+"/hash"
	if os.path.exists(hash_file):
		return open(hash_file,'r').read().replace('\n','')
	else:
		return None

def cmd_in_repo(repo_url, cmd):
	if not cmd: return
	try:
		output = subprocess.check_output("cd "+repo_dir(repo_url)+" && "+cmd,shell=True)
		log.debug("command '{}' output:\n{}".format(cmd,output))
		return output
	except subprocess.CalledProcessError as e:
		log.error("command '{}' with return code {}:\n{}".format(e.cmd,e.returncode,e.output))
		return e.output

def get_repo(repo_url):
	if os.path.isdir(repo_dir(repo_url)) and args.purge:
		os.system("rm -rf "+repo_dir(repo_url))
	if not os.path.isdir(repo_dir(repo_url)):
		print(repo_name(repo_url)+": downloading...")
		os.system("git clone "+repo_url+" "+repo_dir(repo_url))

def apply_patch(repo_url, patch_type):
	patch = patch_file(repo_url, patch_type)
	if patch == None: return False
	print(repo_name(repo_url)+": applying patch... "+ patch)
	hash = repo_hash(repo_url)
	if hash: cmd_in_repo(repo_url,"git checkout -f "+hash)
	else: cmd_in_repo(repo_url,"git stash")
	cmd_in_repo(repo_url,"git clean -fx && git apply "+patch)
	cmd_in_repo(repo_url,"cp ../../local.properties . && chmod +x ./gradlew")
	log.info("Patched "+patch)
	return True

def eval_repo(repo_url, patch_type):
	log.info("Eval Begin - "+repo_name(repo_url)+" - "+patches[patch_type])
	script = patch_prefix+"/"+repo_name(repo_url)+"/"+script_file
	if not os.path.exists(script):
		return cmd_in_repo(repo_url,"./gradlew --no-build-cache build")
	else:
		cmd_in_repo(repo_url,"cp "+os.path.abspath(script)+" . && chmod +x "+script_file)
		return cmd_in_repo(repo_url,"./"+script_file)

def parse_eval(outstr):
	status = filter(None,(line.rstrip() for line in outstr.splitlines()))[-2:]
	if status:
		if " in " in status[0]:
			status_str = status[0]
		else:
			status_str = ";".join(status)
		log.info("--- Eval End : "+status_str)
		if "SUCCESSFUL" in status_str:
			time = re.findall(r"(?:(\d*\.?\d+)(?:m|\smins)\s)?(?:(\d*\.?\d+)(?:s|\ssecs))", status_str)
			if time:
				m,s = time[0]
				return float(m if m else 0)*60 + float(s)
			else: return "-1"
	return "FAIL"

if __name__ == '__main__':
	repos = open(repo_list, 'r').read().splitlines()
	res = open(stats_file,"a",0)
	for url in repos:
		if not url: break
		if url.startswith('#'): continue
		get_repo(url)
		for p in range(len(patches)):
			if apply_patch(url,p):
				outcome = parse_eval(eval_repo(url,p))
				res.write("{},{},{}\n".format(repo_name(url),patches[p],outcome))
	res.close()
