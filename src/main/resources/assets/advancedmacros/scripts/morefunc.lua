--sign( num )
--returns -1, 0 or 1
function math.sign(num)
  if num==0 then
    return 0
  elseif num>0 then
    return 1
  else
    return -1
  end
end


--map(x, iMin, iMax, oMin, oMax)
--maps x from input range to output range
function math.map(x, iMin, iMax, oMin, oMax)
  return (x - iMin) * (oMax - oMin) / (iMax - iMin) + oMin
end