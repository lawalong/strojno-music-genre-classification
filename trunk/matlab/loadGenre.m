function [ array ] = loadGenre( datasetpath, genre )
%LOADGENRE Summary of this function goes here
%   Detailed explanation goes here
array={};
for i=0:99
    d=loadSong(datasetpath, genre,i);
    array = cat(1, array, d);
end

end

