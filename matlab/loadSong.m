function [ d, sr ] = loadSong( datasetpath, genre, index )
%LOAD loads a song from a dataset
%   Detailed explanation goes here

    path=sprintf('%s\\%s\\%s.%05d.au',datasetpath,genre,genre,index);
    [d,sr]=auread(path);
end

