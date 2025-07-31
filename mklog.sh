#!/bin/bash

python eval_repos.py -w -nu $1
rm eval.log
cp -R $2/$1 repos/
cp -R gradle/$1 repos/

rm $2_$1.log compile_args/$1.arg.clean
python eval_repos.py -w -nu $1 > $2_$1.txt
mv eval.log $2_$1.log

