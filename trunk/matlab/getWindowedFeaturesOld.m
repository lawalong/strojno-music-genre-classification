function [ results ] = getWindowedFeatures( d, sr, winlen, functions )
%GETWINDOWEDFEATURES Summary of this function goes here
%   Detailed explanation goes here

    L=round(sr*winlen);
    win=hamming(L);
    results = cell(1,length(functions));
    i=1;
    while (i<length(d)-L/2)
        if (i+L > length(d)); break; end;
        x=win.*d(round(i):round(i+L-1));
        
        ft=abs(fft(x));
        for j=1:length(functions)
            results{j} = [results{j}, functions{j}(x,sr,ft)];
        end
        
        i=i+L/2;
    end

end

