import os.path, ConfigParser
from eval_repos import patches, repo_name, patch_file

repo_list = "eval_repos.txt"
config_file = "gen_patch.conf"
template_suffix = ".tmp"

config = ConfigParser.ConfigParser()
config.optionxform = str
config.read(config_file)

def expand(str, dict):
  for key, value in dict.items():
    str = str.replace(key, value)
  return str

def gen_patch(repo_url, patch_type):
  patch = patch_file(repo_url, patch_type, False)
  template = patch+template_suffix
  if os.path.exists(template):
    with open(template, "r") as fin:
      with open(patch, "w") as fout:
        for line in fin:
          fout.write(expand(line, config.defaults()))
    print("generated: "+patch)

repos = open(repo_list, 'r').read().splitlines()
for url in repos:
  if not url: break
  if url.startswith('#'): continue
  for p in range(len(patches)):
    gen_patch(url,p)
