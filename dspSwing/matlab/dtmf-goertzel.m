% thanks: https://www.st.com/resource/en/design_tip/dm00446805-the-goertzel-algorithm-to-compute-individual-terms-of-the-discrete-fourier-transform-dft-stmicroelectronics.pdf

printf('running ...\n');

function [I,Q] = goertzel(x,k,N)
  w = 2*pi*k/N;
  cw = cos(w); c = 2*cw;
  sw = sin(w);
  z1=0; z2=0; % init
  for n = 1 : N
    z0 = x(n) + c*z1 - z2;
    z2 = z1;
    z1 = z0;
  end;
  I = cw*z1 - z2;
  Q = sw*z1;
endfunction

% DTMF test, dual-tone multi-frequency
frow = [ 697, 770, 852, 941]; % frequencies for 1st tone
fcol = [1209, 1336, 1477, 1633]; % frequencies for 2nd tone
sym = ['1', '4', '7', '*', '2', '5', '8', '0', ... % symbols
 '3', '6', '9', '#', 'A', 'B', 'C', 'D' ];
symrow = [1 2 3 4 1 2 3 4 1 2 3 4 1 2 3 4]; % 1st tone for given symbol
symcol = [1 1 1 1 2 2 2 2 3 3 3 3 4 4 4 4]; % 2nd tone for given symbol
symmtx = [1 5 9 13; 2, 6, 10, 14; 3, 7, 11, 15; 4, 8, 12, 16]; % decoding matrix
Fs = 4000; % Hz, sampling frequency
N = 200; % minimum number of samples per symbol
x = []; % create test signal
symoutref = []; % reference for decoded output

for i=1:length(sym), % test each symbol
  Nsym = N + round(N/2*rand(1)); % samples for current symbol
  t = [0:Nsym-1]/Fs; % time vector
  x1 = sin(2*pi*frow(symrow(i))*t); % first tone
  x2 = sin(2*pi*fcol(symcol(i))*t); % second tone
  x = [x, x1+x2];
  symoutref = [symoutref, sym(i)];
end;
bits = 8;
Q = (max(x)-min(x))/(2^bits); % quantization step for 8 bit signal
x = round(x/Q); % some noise may also be added
fbin=Fs/N; % Goertzel frequency resolution, N must be high enough
k=round([frow fcol]/fbin); % N high enough so that k is different for each tone
if any(diff(k)==0), fprintf('same k index for different tones!\n'); return; end;
% Goertzel-based DTMF decoding
xblocks = floor(length(x)/N);
myspec = []; % zeros(xblocks,length(k));
for i = 1 : xblocks,
  i1 = (i-1)*N+1;
  i2 = i1+N-1;
  xt = x(i1:i2);
  xt = xt.*hamming(N)';
  for j = 1 : length(k),
    [I,Q] = goertzel(xt,k(j),N);
    myspec(i,j) = sqrt(I*I+Q*Q);
  end;
end;
th = max(myspec(:))/2; % threshold
myspecbin = myspec>th; % tone on/off detection
symout = [];
for i = 1 : xblocks,
  i1 = find(myspecbin(i,1:4)>0); if length(i1)~=1, i1=0; end; % 1st tone
  i2 = find(myspecbin(i,5:8)>0); if length(i2)~=1, i2=0; end; % 2nd tone
  if (i1==0) || (i2==0), symdec=' '; % no symbol decoded
  else symdec=sym(symmtx(i1,i2)); % symbol decoded
  end;
  symout = [symout, symdec]; % append decoded symbol
end;

% printout and plot
fprintf('reference string: %s\n',symoutref);
fprintf('decoded string: %s\n',symout);

figure; imagesc(fbin*k/1000,[0:xblocks]*N/Fs*1000,myspec);
axis xy; axis([0 Fs/2/1000 0 length(x)/Fs*1000]); colorbar;
xlabel('Frequency (kHz)'); ylabel('Time (ms)');
title(sprintf('DTMF test, %d-bit Fs=%.1f kHz, %d-point Goertzel',bits,Fs/1000,N));

figure; imagesc(fbin*k/1000,[0:xblocks]*N/Fs*1000,myspecbin);
axis xy; axis([0 Fs/2/1000 0 length(x)/Fs*1000]); colorbar;
xlabel('Frequency (kHz)'); ylabel('Time (ms)');
title(sprintf('DTMF test, %d-bit Fs=%.1f kHz, %d-point Goertzel,th=%1.f',bits,Fs/1000,N,th));
NFFT=128; NOVL=round(0.9*NFFT); WIN=hamming(NFFT);

%figure; spectrogram(x,WIN,NOVL,NFFT,Fs);
%title(sprintf('DTMF test, %d-bit Fs=%.1f kHz, %d-points FFT',bits,Fs/1000,NFFT));

% save quantized test signal to file to test C implementation
h=fopen('in.txt','wt'); fprintf(h,'%d\n',x); fclose(h);