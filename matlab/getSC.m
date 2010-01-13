function sc = getSC( d, sr, ft )
%GETSC gets the spectral centroid of audio
%   Detailed explanation goes here

    if nargin<3;    ft=abs(fft(d)); end

    N = length(ft);
    n = floor(N / 2);
    sc = sum((sr / N) * [1:n]' .* ft(1:n)) / sum(ft(1:n));
end