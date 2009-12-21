function extractFeaturesHammingTest( datasetpath, testname, genres, hammin, hammax, hamn )
%EXTRACTFEATURES extracts all the features of the audio database to a file
%   testname = name of the feature set
%   genres = which genres to include to extraction

    bn = 24;
    fids = {};
    hamw = ones(1,hamn) * hammin + [0:hamn-1] * ((hammax-hammin)/(hamn-1));
    for i=1:hamn
        fids = [fids, fopen(sprintf('extractedFeatures/%s_ham%.2f.arff', testname, hamw(i)), 'w')];
    end
    for i=1:hamn; fprintf(fids{i}, '@RELATION %s_ham%.1f\r\n\r\n', testname, hamw(i)); end;
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
    
    for j=1:hamn; for i=1:length(features); fprintf(fids{j}, '@ATTRIBUTE %s real\r\n', features{i}); end; end;
    
    for j=1:hamn; fprintf(fids{j}, '@ATTRIBUTE class {%s', genres{1}); end;
    for j=1:hamn; 
        for i=2:length(genres)
            fprintf(fids{j}, ',%s', genres{i});
        end
    end
    for j=1:hamn; fprintf(fids{j}, '}\r\n\r\n@DATA\r\n'); end;
    
    
    
    for genre = genres
        for index = 0:99
            [d,sr]=auread(sprintf('%s\\%s\\%s.%05d.au',datasetpath,genre{:},genre{:},index));
            
            bn = 24;
            ftrs1 = getTempo(d,sr);
            res = {};
            for i=1:hamn
                res = [res, {getWindowedFeatures(d, sr, hamw(i), {@getSC, @getSF, @getSR, @getSTE, @getZCR})}];
            end
            mfcc = getMFCC(d,sr);
            [mn, md, mx, band] = getPower(d, sr, bn);
            ftrs2={};
            for j=1:hamn; ftrs2{j}={}; end;
            ftrs3={};
            for j=1:hamn; for i=1:length(res{j}); ftrs2{j} = [ftrs2{j}, std(res{j}{i}), mean(res{j}{i}), median(res{j}{i}), max(res{j}{i})]; end; end;
            for i=1:length(mfcc); ftrs3 = [ftrs3, std(mfcc{i}), mean(mfcc{i}), median(mfcc{i}), max(mfcc{i})]; end;
            ftrs3 = [ftrs3, mn, md, mx];
            ftrs3 = [ftrs3, band];
            
            for i=1:hamn
                fprintf(fids{i}, '%f, ', ftrs1);
                fprintf(fids{i}, '%f, ', ftrs2{i}{:});
                fprintf(fids{i}, '%f, ', ftrs3{:});
                fprintf(fids{i}, '%s\r\n', genre{:});
            end
        end
    end
    for i=1:hamn; fclose(fids{i}); end;
end
