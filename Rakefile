require 'net/ssh'
require 'net/scp'
require 'net/ssh/telnet'

Hostname = "pione.local"
Username = "pi"
UploadFolder = "/home/pi/upload"

itest_bundles = [
  "target-definition/common/repository/plugins/org.hamcrest.core_1.1.0.v20090501071000.jar",
  "target-definition/common/repository/plugins/org.junit_4.10.0.v4_10_0_v20120426-0900.jar",
  "ch.bittailor.iot.core/target/ch.bittailor.iot.core-1.0.0-SNAPSHOT.jar",
  "ch.bittailor.iot.core.integrationtest/target/ch.bittailor.iot.core.integrationtest-1.0.0-SNAPSHOT.jar"
]

app_bundles = [
  "ch.bittailor.iot.core/target/ch.bittailor.iot.core-1.0.0-SNAPSHOT.jar",
  "ch.bittailor.iot.san/target/ch.bittailor.iot.san-1.0.0-SNAPSHOT.jar",
  "ch.bittailor.iot.mqttsn/target/ch.bittailor.iot.mqttsn-1.0.0-SNAPSHOT.jar"
]


task :generate do
  pwd = Dir.pwd
  puts "generate #{pwd}"

  sh "java -jar /Applications/eclipses/luna/eclipse/plugins/org.eclipse.equinox.launcher_*.jar -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher -metadataRepository file:#{pwd}/target-definition/bittailor/repository -artifactRepository file:#{pwd}/target-definition/bittailor/repository/ -source target-definition/bittailor -configs gtk.linux.x86 -publishArtifacts"

end

task :build do
  sh "mvn clean verify"
end

task :itest_deploy => :build do
  upload(itest_bundles)
end

task :app_deploy => :build do
  upload(app_bundles)
end

task :itest_run => :itest_deploy do
  failed_tests = 0;
  OsgiConsole.open do |console|
    bundle_ids = []
    puts "install #{itest_bundles.count} itest_bundles"
    itest_bundles.each do |bundle|
      console.exec("install file:#{UploadFolder}/#{File.basename(bundle)}") do |line|
        # puts line
        m = /Bundle id is (\d+)/.match(line)
        if m
          bundle_ids << m[1]
        end
      end
    end
    puts "start #{itest_bundles.count} itest_bundles"
    bundle_ids.each do |id|
      console.exec("start #{id}") do |line|
        puts line
        m = /!!! (\d+) TESTS FAILED !!!/.match(line)
        if m
          failed_tests = failed_tests + m[1].to_i
        end
      end
    end
    puts "uninstall #{itest_bundles.count} itest_bundles"
    bundle_ids.reverse.each do |id|
      console.exec("uninstall #{id}") do |line|
        #puts line
      end
    end
  end
  fail "#{failed_tests} tests failed!"   if failed_tests > 0
end

task :app_run => :app_deploy do
  OsgiConsole.open do |console|
    bundle_ids = []
    puts "install #{app_bundles.count} app_bundles"
    app_bundles.each do |bundle|
      console.exec("install file:#{UploadFolder}/#{File.basename(bundle)}") do |line|
        # puts line
        m = /Bundle id is (\d+)/.match(line)
        if m
          bundle_ids << m[1]
        end
      end
    end
    bundle_ids.each do |id|
      console.exec("start #{id}") do |line|
        puts line
      end
    end
    puts "bundles #{bundle_ids.join(',')}"
  end
end

task :default => :itest_run


def upload(bundles)
  puts "login to #{Hostname}"
  Net::SCP.start(Hostname, Username) do |scp|
    bundles.each do |bundle|
      puts "upload #{bundle}"
      scp.upload!(bundle,UploadFolder);
    end
  end
end

class OsgiConsole

  def self.open()
    console = new()
    begin
       yield console
     ensure
       console.close
     end
  end

  def initialize()
    puts "login to #{Hostname}"
    @ssh = Net::SSH.start(Hostname, Username)
    @channel = @ssh.open_channel do |channel|
      puts "open osgi console on #{Hostname}"
      channel.exec("telnet localhost 5002") do |ch, success|
        abort "could not execute command" unless success
        channel.on_data do |ch, data|
          process(data)
        end

        channel.on_extended_data do |ch, type, data|
          # puts "[rake stderr] #{data}"
        end

        channel.on_close do |ch|
          @state = :closed
          # puts "[rake channel is closing!]"
        end
      end
    end
    @state = :waiting
    @ssh.loop() { @state != :ready }
  end

  def process(data)
    lines = data.split("\n")
    lines.each do |line|
      if line.start_with?("osgi>")
        @state = :ready;
      else
        @callback.call(line) if @callback
        @lines << line if @lines
      end
    end
  end

  def exec(cmd, &block)
    @lines = []
    if block_given?
      @callback = block
    else
      @callback = nil
    end
    @state = :waiting
    @channel.send_data("#{cmd}\n")
    @ssh.loop() { @state != :ready }
    return @lines
  end

  def close()
    @state = :waiting
    @channel.send_data("disconnect\ny\n")
    @ssh.loop() { @state != :closed }
    @ssh.close()
  end

end
