function extractSongsFeatures( stdout, relation, songpaths, genres )
%EXTRACTSONGSFEATURES Summary of this function goes here
%   Detailed explanation goes here

    fprintf(stdout, '@RELATION %s\r\n\r\n', relation);
	
    bn = 24;
    features = {'BPM1', 'BPM2', 'BPM1pow', 'stdSC', 'meanSC', 'medSC', 'maxSC', 'stdSF', 'meanSF', 'medSF', 'maxSF', 'stdSR', 'meanSR', 'medSR', 'maxSR', 'stdSTE', 'meanSTE', 'medSTE', 'maxSTE', 'stdZCR', 'meanZCR', 'medZCR', 'maxZCR'};
    for i=1:5; features=[features, sprintf('stdMFCC_%d', i), sprintf('meanMFCC_%d', i), sprintf('medianMFCC_%d', i), sprintf('maxMFCC_%d', i)]; end;
    features=[features, 'meanPower', 'medPower', 'maxPower'];
    sr = 22050;
    fMaxL = log2(sr/2);
    borderDown = log2(20);
    bandWidth = (fMaxL - borderDown) / bn;
    for i=1:bn
        borderUp = borderDown + bandWidth;
        downFreq = round(pow2(borderDown));
        upFreq = round(pow2(borderUp));
        features = [features, sprintf('freqpow_%d-%dHz', downFreq, upFreq)];
        borderDown = borderUp;
    end
    
    for i = 1:length(features)
        fprintf(stdout, '@ATTRIBUTE %s real\r\n', features{i});
    end
    
    fprintf(stdout, '@ATTRIBUTE class {%s', genres{1});
    for i = 2:length(genres)
        fprintf(stdout, ',%s', genres{i});
    end
    fprintf(stdout, '}\r\n\r\n@DATA\r\n');
    
    for songpath = songpaths
        [d, sr] = auread(songpath{:});
        ftrs = extractSongFeatures(d, sr);
        fprintf(stdout, '%f, ', ftrs);
        fprintf(stdout, '?\r\n');
    end

end

