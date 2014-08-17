#!
#keyboard in german
cp /vagrant/data/keyboard /etc/default/keyboard
#Load Keyboard changes
udevadm trigger --subsystem-match=input --action=change

#install puppet
apt-get install -y puppet-common
