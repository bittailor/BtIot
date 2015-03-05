

task :generate do
  pwd = Dir.pwd
  puts "generate #{pwd}"

  sh "java -jar /Applications/eclipses/luna/eclipse/plugins/org.eclipse.equinox.launcher_*.jar -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher -metadataRepository file:#{pwd}/target-definition/bittailor/repository -artifactRepository file:#{pwd}/target-definition/bittailor/repository/ -source target-definition/bittailor -configs gtk.linux.x86 -publishArtifacts"

end

task :default => :generate
