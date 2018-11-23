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