# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant::Config.run do |config|
  config.vm.define :server do |server_config|
    server_config.vm.box = "base"
    server_config.vm.provision :chef_solo do |chef|
      chef.cookbooks_path = "cookbooks"
      chef.add_recipe "vagrant_main"
      chef.add_recipe "java"
      chef.add_recipe "maven"
    end

    server_config.vm.network :hostonly, "192.168.2.10"
    server_config.vm.forward_port 8888, 12306
  end

  config.vm.define :client do |client_config|
    client_config.vm.box = "base"
    client_config.vm.provision :chef_solo do |chef|
      chef.cookbooks_path = "cookbooks"
      chef.add_recipe "vagrant_main"
      chef.add_recipe "java"
      chef.add_recipe "maven"
    end

    client_config.vm.network :hostonly, "192.168.2.11"
    client_config.vm.forward_port 9999, 12307
  end
end
