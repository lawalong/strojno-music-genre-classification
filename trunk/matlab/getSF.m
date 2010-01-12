function sf = getSF( d, sr, ft )
%GETSF Summary of this function goes here
%   Detailed explanation goes here

    if nargin<2;    ft=abs(fft(d)); end
    
    N = length(d);
    sf = sum((ft(2:N)-ft(1:N-1)).^2);
end

