#! /bin/bash
KEYBOARD=macbook

echo "Setze $KEYBOARD keyboard"
sudo cp /etc/default/keyboard_$KEYBOARD /etc/default/keyboard

sudo udevadm trigger --subsystem-match=input --action=change

