function zcr = getZCR( d, sr, ft )
%GETZCR Summary of this function goes here
%   Detailed explanation goes here

    N = length(d);
    T = N / sr;
    zcr = 0;
    for i = 2:N
        zcr = zcr + abs((sign(d(i))-sign(d(i-1))) / 2);
    end
    zcr = zcr / T;
end

