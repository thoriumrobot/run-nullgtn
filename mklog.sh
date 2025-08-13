#!/bin/bash

cp -R $2/$1 repos/
cp -R gradle/$1 repos/

rm $2_$1.log compile_args/$1.arg.clean
python eval_repos.py -w -nu $1 > $2_$1.txt
mv eval.log $2_$1.log

file="${2}_${1}.log"

# count occurrences of "[nullaway"
open_count=$(grep -Eio '\[nullaway' "$file" | wc -l)

# count occurrences of "nullaway]"
close_count=$(grep -Eio 'nullaway\]' "$file" | wc -l)

# count occurrences of "[nullaway]" (which both of the above also match)
both_count=$(grep -Eio '\[nullaway\]' "$file" | wc -l)

# compute union: open_count + close_count − both_count
total=$(( open_count + close_count - both_count ))

echo "Warnings for $1 annotated by $2 = $total"

