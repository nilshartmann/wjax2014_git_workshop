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

exec { "accept java license":
  command => "/bin/echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections"
}

package { 'oracle-java8-installer' :
  ensure => installed,
  require => Exec['accept java license'],
}

package { 'groovy':
  ensure => 'installed',
  require => Package['oracle-java8-installer'],
}

package { 'maven':
  ensure => 'installed',
  require => Package['oracle-java8-installer'],
}

package { 'gradle':
  ensure => 'installed',
  require => Package['oracle-java8-installer'],
}

package { 'git':
  ensure => 'installed'
}

package { 'git-gui':
  ensure => 'installed',
  require => Package['git'],
}

package { 'git-cola':
  ensure => 'installed',
  require => Package['git'],
}

# Grafisches Mergetool
package { 'meld':
  ensure => 'installed',
  require => Package['git']
}

file { ['/usr/share/git-gui/lib/msgs/de.msg','/usr/share/gitk/lib/msgs/de.msg']:
    ensure  => absent,
    require => Package['git-gui'],
}

file { 'gitconfig':
	path => '/home/vagrant/.gitconfig',
	ensure => 'file',
	source => '/vagrant/data/gitconfig'
}

package { 'git-flow':
  ensure => 'installed',
  require => [ Package['git'], File['gitconfig'] ]
}

package { 'atom':
  ensure => 'installed'
}

package { 'cmake':
    ensure   => 'installed',
}

package { 'rugged':
    ensure   => 'installed',
    provider => 'gem',
    require => Package['cmake'],
}


Apt::Ppa['ppa:git-core/ppa'] -> Package['git','git-gui']
Apt::Ppa['ppa:cwchien/gradle'] -> Package['gradle']
Apt::Ppa['ppa:webupd8team/atom'] -> Package['atom']
Apt::Ppa['ppa:webupd8team/java'] -> Package['oracle-java8-installer']

####### SSH Files ####################################
file { "/home/vagrant/.ssh":
    ensure => "directory",
 	recurse => "remote",
	purge => false,
	source => "/vagrant/data/ssh"
}

####### Eclipse ######################################
archive { 'eclipse':
  ensure => present,
  url    => 'http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/luna/SR1/eclipse-java-luna-SR1-linux-gtk.tar.gz',
  target => '/opt',
  checksum => false,
  timeout => 0,
  src_target => '/tmp',
  require => Package['oracle-java8-installer'],
}

exec { "install gradle eclipse tooling":
	command => "/opt/eclipse/eclipse -application org.eclipse.equinox.p2.director -consolelog -noSplash -repository http://dist.springsource.com/release/TOOLS/gradle -installIU org.springsource.ide.eclipse.gradle.feature.feature.group",
  require => Archive['eclipse'],
}

exec { "install groovy eclipse tooling":
  command => "/opt/eclipse/eclipse -application org.eclipse.equinox.p2.director -consolelog -noSplash -repository http://dist.springsource.org/milestone/GRECLIPSE/e4.4/ -installIU org.codehaus.groovy.eclipse.feature.feature.group",
  require => Archive['eclipse'],
}

######## Tutorial Repositories #########################
exec { "get all repos":
  cwd => "/vagrant/data/clonerepos",
  user => "vagrant",
  command => "/usr/bin/gradle -PrepoDirRoot=/home/vagrant/repos all",
  require => [Package['git'], Package['maven'], Package['gradle']],
  timeout => 0
}

###### Initiales Laden der Maven-Dependencies sowie einrichten der 
######  Maven- und Git "Release" Repositories
exec { "init maven and gradle dependencies":
	cwd => "/tmp",
	user => "vagrant",
	command => "/vagrant/data/init-maven-and-gradle.sh",
	require => Exec['get all repos']
}

###### Desktop Verknüpfungen #############################

file { "/home/vagrant/Schreibtisch":
    ensure => "directory",
    owner  => "vagrant",
    group  => "vagrant",
}

file { "/home/vagrant/Schreibtisch/terminal.desktop":
    mode => 775,
    ensure => "present",
    source => "/vagrant/data/terminal.desktop",
    owner  => "vagrant",
    group  => "vagrant"
}
->
file { "/home/vagrant/Schreibtisch/eclipse.desktop":
    mode => 775,
    ensure => "present",
    source => "/vagrant/data/eclipse.desktop",
    owner  => "vagrant",
    group  => "vagrant",
    require => Archive['eclipse'],
}
->
file { "/home/vagrant/Schreibtisch/atom.desktop":
    mode => 775,
    ensure => "present",
    source => "/vagrant/data/atom.desktop",
    owner  => "vagrant",
    group  => "vagrant",
    require => Package['atom']
}
->
file { "/home/vagrant/Schreibtisch/macbook-keyboard.desktop":
    mode => 775,
    ensure => "present",
    source => "/vagrant/data/macbook-keyboard.desktop",
    owner  => "vagrant",
    group  => "vagrant"
}
