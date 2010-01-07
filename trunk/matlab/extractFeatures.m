function extractFeatures( stdout, datasetpath, testname, genres )
%EXTRACTFEATURES extracts all the features of the audio database to a file
%   testname = name of the feature set
%   genres = which genres to include to extraction

    bn = 24;
    fid = stdout;
    fprintf(fid, '@RELATION %s\r\n\r\n', testname);
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
        fprintf(fid, '@ATTRIBUTE %s real\r\n', features{i});
    end
    
    fprintf(fid, '@ATTRIBUTE class {%s', genres{1});
    for i = 2:length(genres)
        fprintf(fid, ',%s', genres{i});
    end
    fprintf(fid, '}\r\n\r\n@DATA\r\n');
    
    
    
    for genre = genres
        files = dir([datasetpath '/' genre{:}]);
        for i = 1 : length(files)
            filename = files(i).name;
            if length(filename) < 3 || ~strcmp('.au', filename(length(filename)-2:length(filename))); continue; end;
            [d,sr]=auread([datasetpath '/' genre{:} '/' filename]);
            %[d,sr]=auread(sprintf('%s\\%s\\%s.%05d.au',datasetpath,genre{:},genre{:},index));
            ftrs = extractSongFeatures(d,sr);
            fprintf(fid, '%f, ', ftrs);
            fprintf(fid, '%s\r\n', genre{:});
        end
    end
    fclose(fid);
end
