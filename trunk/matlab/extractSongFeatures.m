function features = extractSongFeatures( d, sr )
%EXTRACTFEATURES extracts all the features of one audio file
	bn = 24;
    features = getTempo(d,sr);
    res = getWindowedFeatures(d, sr, 1, {@getSC, @getSF, @getSR, @getSTE, @getZCR});
    mfcc = getMFCC(d,sr);
    [mn, md, mx, band] = getPower(d, sr, bn);
    for i=1:length(res); features = [features, std(res{i}), mean(res{i}), median(res{i}), max(res{i})]; end;
    for i=1:length(mfcc); features = [features, std(mfcc{i}), mean(mfcc{i}), median(mfcc{i}), max(mfcc{i})]; end;
    features = [features, mn, md, mx];
    features = [features, band];
end
