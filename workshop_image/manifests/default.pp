class { 'apt':
 always_apt_update => true,
}

apt::ppa { 'ppa:git-core/ppa':}
apt::ppa { 'ppa:cwchien/gradle':}
apt::ppa { 'ppa:webupd8team/atom':}

package { 'openjdk-7-jdk' :
  ensure => installed
}

package { 'git':
  ensure => 'installed'
}

package { 'git-gui':
  ensure => 'installed'
}

package { 'gradle':
  ensure => 'installed'
}

package { 'maven2':
  ensure => 'installed'
}

package { 'atom':
  ensure => 'installed'
}


Apt::Ppa['ppa:git-core/ppa'] -> Package['git','git-gui']
Apt::Ppa['ppa:cwchien/gradle'] -> Package['gradle']
Apt::Ppa['ppa:webupd8team/atom'] -> Package['atom']

archive { 'eclipse':
  ensure => present,
  url    => 'http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/luna/R/eclipse-java-luna-R-linux-gtk-x86_64.tar.gz',
  target => '/opt',
  checksum => false,
  timeout => 0,
  src_target => '/tmp'
}
->
file { "/home/vagrant/Desktop/eclipse.desktop": 
    mode => 775,
    ensure => "present",
    source => "/vagrant/data/eclipse.desktop",
}
