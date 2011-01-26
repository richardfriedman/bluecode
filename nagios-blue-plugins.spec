Summary: Blue Java Plugins, Blue is a port of Nagios to Java
Name: nagios-blue-plugins
Version: 0.8
Release: 1
License: GPL
Group: Applications/System
URL: http://blue.sourceforge.net/
Vendor: Blue
Packager: Richard Friedman <richardfriedman@yahoo.com>
Prefix: %{_prefix}/lib/nagios/plugins
BuildArchitectures: noarch

%description
Blue® is a java port of the popular host and service monitor system Nagios®. 
Project Blue will make available Blue Server, Blue Console, Blue Plugins 
and Blue Agent. It is the intention that each of the components will co-exist 
with all nagios components. Run Blue Server with the existing Nagios Console 
and Nagios Plugins (all combinations supported). By moving to a java platform 
developing, extending, designing of the system as a platform simplifies things, 
at least for some folks! The project has already ported the nagios server code 
and nagios cgis (to servlets).

%prep

%build

%install

%files
/usr/lib/nagios/plugins/
