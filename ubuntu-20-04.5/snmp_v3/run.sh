#!/bin/bash

# Post sandbox usage to Pluto. Connection to AppGate required.
script_path="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
sandbox_dir=$(echo $script_path | sed -n 's:sandbox/.*:sandbox:p')
sh $sandbox_dir/usage/usage.sh $script_path $(basename $BASH_SOURCE)
if [ $? -ne 0 ] ; then
    echo "Please connect to AppGate and retry"
    exit 1
fi
echo "Sandbox usage sent to Supportability Engineering. Thanks!"


if [[ $(arch) == "arm64" ]]; then
  echo "Running VAGRANT_VAGRANTFILE=Vagrantfile.arm64 vagrant $1"
  VAGRANT_VAGRANTFILE=Vagrantfile.arm64 vagrant $1
else
  echo "Running VAGRANT_VAGRANTFILE=Vagrantfile.intel vagrant $1"
  VAGRANT_VAGRANTFILE=Vagrantfile.intel vagrant $1
fi