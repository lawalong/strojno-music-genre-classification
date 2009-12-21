function str = arr2str( arr )
%ARR2STR Summary of this function goes here
%   Detailed explanation goes here
    str = sprintf('%f', arr(1));
    for i=2:length(arr); str = strcat(str, sprintf(', %f', arr(i))); end;
end

