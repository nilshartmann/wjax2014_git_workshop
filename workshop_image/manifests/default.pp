class { 'apt':
 always_apt_update => true,
}

class { 'ruby':
  gems_version  => 'latest'
}

apt::ppa { 'ppa:git-core/ppa':}
apt::ppa { 'ppa:cwchien/gradle':}
apt::ppa { 'ppa:webupd8team/atom':}
apt::ppa { 'ppa:webupd8team/java':}

package { 'oracle-java8-installer' :
  ensure => installed
}
->
package { 'groovy':
  ensure => 'installed'
}
->
package { 'maven2':
  ensure => 'installed'
}
->
package { 'gradle':
  ensure => 'installed'
}

package { 'git':
  ensure => 'installed'
}

package { 'git-gui':
  ensure => 'installed'
}

package { 'atom':
  ensure => 'installed'
}

package { 'cmake':
    ensure   => 'installed',
}
->
package { 'rugged':
    ensure   => 'installed',
    provider => 'gem',
}


Apt::Ppa['ppa:git-core/ppa'] -> Package['git','git-gui']
Apt::Ppa['ppa:cwchien/gradle'] -> Package['gradle']
Apt::Ppa['ppa:webupd8team/atom'] -> Package['atom']
Apt::Ppa['ppa:webupd8team/java'] -> Package['oracle-java8-installer']

exec { "accept java license":
  command => "/bin/echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections"
}

Exec['accept java license']->Package['oracle-java8-installer']

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
->
exec { "install gradle eclipse tooling":
	command => "/opt/eclipse/eclipse -application org.eclipse.equinox.p2.director -consolelog -noSplash -repository http://dist.springsource.com/release/TOOLS/gradle -installIU org.springsource.ide.eclipse.gradle.feature.feature.group"
}
->
exec { "install groovy eclipse tooling":
  command => "/opt/eclipse/eclipse -application org.eclipse.equinox.p2.director -consolelog -noSplash -repository http://dist.springsource.org/milestone/GRECLIPSE/e4.4/ -installIU org.codehaus.groovy.eclipse.feature.feature.group"
}

Package['oracle-java8-installer'] -> Archive['eclipse']

exec { "get all repos":
  cwd => "/vagrant/data/clonerepos",
  user => "vagrant",
  command => "/usr/bin/gradle -PrepoDirRoot=/home/vagrant/repos all"
}

Package['gradle'] -> Exec['get all repos']
