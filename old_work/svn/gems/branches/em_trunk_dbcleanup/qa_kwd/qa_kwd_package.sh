#!/bin/sh
rm -rf testing_infra
mkdir testing_infra
rsync -avHx --exclude=".svn" scripts testing_infra/
rsync -avHx --exclude=".svn" test_cases testing_infra/
rsync -avHx --exclude=".svn" kwd/target/kwd-jar-with-dependencies.jar testing_infra/
tar -cvzf testing_infra.tar testing_infra/
