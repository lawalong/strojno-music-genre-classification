function rolloff = getSR( d, sr, ft )
%GETSR Summary of this function goes here
%   Detailed explanation goes here

    if nargin<3;    ft=abs(fft(d)); end
    
    ft = ft(1:floor(length(d)/2));
    border = 0.85 * sum(ft);
    s = 0;
    for i = 1:length(ft)
        s = s + ft(i);
        if s >= border
            rolloff = i * sr / length(d);
            return
        end
    end
end