function [ results ] = getWindowedFeatures( d, sr, winlens, functions )
%GETWINDOWEDFEATURES Summary of this function goes here
%   Detailed explanation goes here

    hamMap = struct;
    results = cell(1,length(functions));
    Ls=round(sr*winlens);
    
    for i=1:length(winlens)
        
        if (isfield(hamMap, ['a' num2str(winlens(i)*10)]) == 0)
            L = Ls(i);
            xLength = floor(2 * length(d) / L) - 1;
            win=hamming(L);
            x = cell(1, xLength);
            ft = cell(1, xLength);
            
            for j=1:xLength
                x{j} = win .* d(floor((j-1)*(L/2))+1:floor((j+1)*(L/2)));
                ft{j} = abs(fft(x{j}));
            end
            
            hamMap.(['a' num2str(winlens(i)*10)]) = {x, ft};
        end
        ham = hamMap.(['a' num2str(winlens(i)*10)]);
        x = ham(1);
        ft = ham(2);
        for j=1:length(x)
            results{i} = [results{i}, functions{i}(x{1}{j}, sr, ft{1}{j})];
        end
    end

end

