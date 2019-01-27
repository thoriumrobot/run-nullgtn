Performance Benchmarking of Java Null Safety Tools
------------
Evaluates build times for Java projects using [NullAway](https://github.com/uber/NullAway), [Checker Framework](https://github.com/typetools/checker-framework)'s [Nullness Checker](https://checkerframework.org/manual/#nullness-checker), and [Eradicate](https://fbinfer.com/docs/eradicate.html).
___
### Setup ###
  * JDK 8 TODO
___
### Usage ###
```
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
### Troubleshooting ###
If all builds of a project fail, the Gradle build cache is likely invalidated. To fix this, re-capture the Gradle compile tasks' arguments:
```
python javac_args.py [projects]
```
