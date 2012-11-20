# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant::Config.run do |config|
  config.vm.box = "base"

  config.vm.provision :chef_solo do |chef|
    chef.cookbooks_path = "cookbooks"
    chef.add_recipe "vagrant_main"
    chef.add_recipe "java"
    chef.add_recipe "maven"
    chef.add_recipe "tomcat"
  end

  config.vm.forward_port 8080, 12306
end
