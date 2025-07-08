#!/bin/bash

cp -R $2/$1 repos/

conda activate naenv
rm eval.log compile_args/$1.arg.clean
python eval_repos.py -w -nu $1 > $2_$1.txt
mv eval.log $2_$1.log

