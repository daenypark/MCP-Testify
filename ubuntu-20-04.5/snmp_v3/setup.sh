#!/bin/bash

auth_key_string=dd_auth_2024
priv_key_string=dd_priv_2024
snmpv3_user=dd-user_2024

. ~/.sandbox.conf.sh

echo "Provisioning!"
echo ""

echo "apt-get updating"
sudo apt-get update -y
sudo apt-get upgrade -y
echo "install curl if not there..."
sudo apt-get install -y curl

echo "Installing dd-agent from api_key: ${DD_API_KEY}..."
DD_API_KEY=${DD_API_KEY} DD_SITE="datadoghq.com" bash -c "$(curl -L https://s3.amazonaws.com/dd-agent/scripts/install_script_agent7.sh)"
sudo apt install snmpd snmp libsnmp-dev -y

sudo cp /etc/snmp/snmpd.conf{,.bak}
sudo apt install net-tools -y
sudo systemctl stop snmpd 
sudo cp /usr/bin/net-snmp-create-v3-user ~/
sudo sed -ie '/prefix=/adatarootdir=${prefix}\/share' /usr/bin/net-snmp-create-v3-user
sudo net-snmp-create-v3-user -ro -A $auth_key_string -a SHA -X $priv_key_string -x AES $snmpv3_user

sudo ufw allow from 127.0.0.1 to any port 161 proto udp comment "Allow SNMP Scan from Monitoring Server"

sudo systemctl start snmpd
sudo systemctl enable snmpd

echo -e "\n    user: $snmpv3_user\n    authKey: $auth_key_string\n    privKey: $priv_key_string" >> /home/vagrant/data/conf.yaml
sudo cp /home/vagrant/data/conf.yaml /etc/datadog-agent/conf.d/snmp.d/

sudo systemctl restart datadog-agent

echo "run the command: export auth_key_string=$auth_key_string; export priv_key_string=$priv_key_string;export snmpv3_user=$snmpv3_user" 