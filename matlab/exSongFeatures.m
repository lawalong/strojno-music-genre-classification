function features = exSongFeatures(songPath)
    [d, sr] = auread(songPath);
    features = extractSongFeatures(d, sr);
end