#!/bin/bash
grep -q "dport $1" /etc/iptables.rules
a=$?
echo $a
if [ $a == 0 ];
then
        echo "Already Exists"
else
##                sudo sed --in-place '/COMMIT/d' /etc/iptables.rules
                sudo sed -i '/INPUT -i eth0 -p udp -m udp -j ACCEPT/i -A INPUT -i eth0 -p tcp -m tcp --dport '$1' -j ACCEPT' /etc/iptables.rules
                sudo sed -i '/INPUT -i eth0 -j DROP/i -A INPUT -i eth0 -p tcp -m tcp --sport '$1' -m state --state ESTABLISHED -j ACCEPT' /etc/iptables.rules
                sudo sed -i '/COMMIT/i -A OUTPUT -o eth0 -p tcp -m tcp --dport '$1' -m state --state NEW,ESTABLISHED -j ACCEPT' /etc/iptables.rules
##              echo "-A INPUT -i eth0 -p tcp -m tcp --dport $1 -j ACCEPT" | sudo tee -a /etc/iptables.rules > /dev/null
##                echo "-A INPUT -i eth0 -p tcp -m tcp --sport $1 -m state --state ESTABLISHED -j ACCEPT" | sudo tee -a /etc/iptables.rules > /dev/null
##                echo "-A OUTPUT -o eth0 -p tcp -m tcp --dport $1 -m state --state NEW,ESTABLISHED -j ACCEPT" | sudo tee -a /etc/iptables.rules > /dev/null
##                echo "COMMIT"  | sudo tee -a /etc/iptables.rules > /dev/null
                sudo /etc/init.d/networking restart
fi

