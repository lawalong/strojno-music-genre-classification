function [ mn, med, mx, bandpow ] = getPower( d, sr, bn, ft )
%GETPOWER gets power features from audio
%   mn = mean power
%   med = median power
%   mx = max power
%   bandpow = logarithmic band power distribution (bn equals number of bands to divide the audio on)

    if nargin<3;    bn=24;   end
    if nargin<4;    ft=abs(fft(d)); end
    fMin = 20;
    bandpow = [];
    N = length(d);
    bar = round(fMin * N / sr);
    
    p = ft;
    p = p(1+bar:N/2).^2;
    n = length(p);
    fMaxL = log2(sr/2);
    borderDown = log2(fMin);
    bandWidth = (fMaxL - borderDown) / bn;
    
    for i = 1:bn
        borderUp = borderDown + bandWidth;
        downIndex = round(pow2(borderDown) * 2 * n / sr);
        upIndex = round(pow2(borderUp) * 2 * n / sr);
        bandpow(i) = sum(p([downIndex:upIndex]));
        borderDown = borderUp;
    end
    
    mn = mean(p);
    med = median(p);
    mx = max(p);
end

