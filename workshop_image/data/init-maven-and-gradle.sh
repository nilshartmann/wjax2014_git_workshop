#! /bin/bash

sudo rm -rf /opt/workshop
sudo mkdir -p /opt/workshop/mvn-repo
sudo mkdir -p /opt/workshop/git-repos

sudo chmod -R 777 /opt/workshop/

cd /tmp
rm -rf /tmp/web-application
git clone /home/vagrant/repos/submodule_subtree/submodule/web-application.git
cd web-application
gradle jettyRunDaemon

rm -rf /tmp/web-application

cd /tmp
cp -r /home/vagrant/repos/build/maven-rel-plugin/web-application.git/ /opt/workshop/git-repos
git clone /home/vagrant/repos/build/maven-rel-plugin/web-application.git
cd /tmp/web-application
mvn --batch-mode release:prepare
mvn release:perform

# Release wieder entfernen
rm -rf /opt/workshop/mvn-repo/de
rm -rf /opt/workshop/git-repos/web-application.git
cp -r /home/vagrant/repos/build/maven-rel-plugin/web-application.git/ /opt/workshop/git-repos


