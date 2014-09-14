#!
#keyboard in german

echo KEYBOARD WIRD KOPIERT

cp /vagrant/data/keyboard /etc/default/keyboard
cp /vagrant/data/keyboard /etc/default/keyboard_german
#keyboard macbook
cp /vagrant/data/keyboard_macbook /etc/default/keyboard_macbook

cp /vagrant/data/set-keyboard-macbook.sh /usr/local/bin/set-keyboard-macbook.sh
chmod 775 /usr/local/bin/set-keyboard-macbook.sh

#Load Keyboard changes
udevadm trigger --subsystem-match=input --action=change

#install puppet
apt-get install -y puppet-common
