# SNMP V3


## What this sandbox does:
- Creates a `ubuntu` `vm` with `snmp` configured for version 3.

## VM type: ubuntu / vagrant

## SNMP V3 Flags:
```
authkey=SHA
privkey=AES
```

## 1. Configure in `setup.sh` file the `VALUE` placeholders (minimum 8 characters):
```
auth_key_string=<VAlUE>
priv_key_string=<VAlUE>
snmpv3_user=<VAlUE>
```

## 2. Spin up Sandbox:
```
./run.sh up;
./run.sh ssh
```
- ### snmp `yaml` configuration will reflect your `VALUE` set:
```
init_config:
    loader: core
    use_device_id_as_hostname: true
instances:
  -
    ip_address: localhost
    snmp_version: 3
    loader: core
    use_device_id_as_hostname: true
    authProtocol: SHA
    privProtocol: AES
    tags:
      - minor:jammy
    user: $snmpv3_user
    authKey: $auth_key_string
    privKey: $priv_key_string
```

## 3. To run `snmpwalk` commands:

#### 3a. Use the `<VALUE>` set in `setup.sh` file:
```
$auth_key_string=<AUTH_VALUE>
$priv_key_string=<PRIV_VALUE>
$snmpv3_user=<USER_VALUE>
```

#### 3b. Or set `Environment Variables` on `vm`:
```
export auth_key_string=<VALUE>;
export priv_key_string=<VALUE>;
export snmpv3_user=<VALUE>
```

## 4. `snmpwalk` commands:
```
#HOST COMMAND
snmpwalk -v 3 -a SHA -A $auth_key_string -x AES -X $priv_key_string -l authPriv -u $snmpv3_user localhost:161

#AGENT COMMAND
sudo datadog-agent snmp walk localhost:161 1.3 -v 3 -a SHA -A $auth_key_string -x AES -X $priv_key_string -l authPriv -u $snmpv3_user  
```

##  `snmp` metrics from VM should begin coming into Datadog