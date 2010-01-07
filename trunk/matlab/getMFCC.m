function features = getMFCC(d, sr)
    % 5 prvih koeficijenata MFCC
    [mm,aspc] = melfcc(d*3.3752, sr, 'maxfreq', 8000, 'numcep', 5, 'nbands', 19, 'fbtype', 'fcmel', 'dcttype', 1, 'usecmp', 1, 'wintime', 0.032, 'hoptime', 0.016, 'preemph', 0, 'dither', 1);
    features = {};
    for i=1:size(mm)
       features = [features, mm(i, 1:end)]; 
    end
end