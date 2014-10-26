require 'rugged'

#Open Repo
repo = Rugged::Repository.new('.')
object = repo.lookup(repo.head.target.oid)
puts object.message

File.open("foo.txt", 'a+') { |f| f.write("next line\n") }

#Work with Index
index=repo.index
index.add('foo.txt')
index.each { |i| puts i.inspect }
tree=index.write_tree()
index.write()

#Create new Commit
author = {:email => 'rp@etosquare.de', :time => Time.now, :name => 'Rene Preissel'}
parents = [repo.head.target.oid]

commit = Rugged::Commit.create(
    repo,
    :author => author,
    :message => "Hello world",
    :committer => author,
    :parents => parents,
    :tree => tree,
    :update_ref => 'HEAD')

puts commit
