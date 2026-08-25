Sound files go here.

The game looks for AUDIO/<name>.ogg, then .mp3, then .wav, and stays silent for
any name it cannot find, so you can add these one at a time.

Music (looped, set by the Music volume slider)
  menu               main menu and all the menu screens          [shipped]
  battle             an ordinary level, and the fallback for
                     any chapter without its own track           [shipped]
  battle-egypt       Ancient Egypt
  battle-frostbite   Frost Bite                                  [shipped]
  battle-waveybeach  Wavey Beach                                 [shipped]
  battle-darkages    Dark Ages                                   [shipped]
  boss               a Zomboss level, for any chapter            [shipped]
  boss-egypt         the Robot Zomboss
  boss-frostbite     the Mammoth Zomboss
  boss-waveybeach    the Shark Zomboss
  boss-darkages      the Dragon Zomboss
  minigame           the minigames and the scoring game          [shipped]
  duel               the two player I, Zombie duel               [shipped]
  zen                the Zen Garden                              [shipped]

A chapter track is optional. The battle music is chosen by trying
battle-<chapter> first and falling back to battle, and boss music the same way,
so dropping in battle-egypt.ogg is all it takes to give Egypt its own theme.
The chapter part of the name is the chapter with spaces and punctuation removed
and everything lower case.

Effects (one shot, set by the Sound volume slider)
  plant         a plant is planted
  shovel        a plant is dug up
  shoot         a straight or piercing shot leaves a plant
  lob           a pult throws its cabbage, kernel or melon
  splat         a shot lands on a zombie
  explode       a cherry bomb, jalapeno, grapeshot or dynamite goes off
  gulp          plant food is eaten
  mint          a mint wakes its family
  sun           sun is collected
  chime         a scoring bonus is earned                        [shipped]
  bite          a zombie starts eating a plant
  plant-dies    a plant is lost
  zombie-dies   a zombie falls over
  armour-breaks a cone, bucket, brick or newspaper is destroyed
  grave-breaks  a gravestone is cleared
  rise          a zombie climbs out of a grave or the low tide
  storm         a sandstorm carries a zombie
  tide          the beach tide moves
  mower         a lawn mower starts rolling
  wave          a new wave arrives
  huge-wave     the final wave arrives
  boss-move     the Zomboss starts an attack
  boss-hurt     the Zomboss loses a health segment
  click         any button in any menu
  win           a level is won                                   [shipped]
  lose          a level is lost                                  [shipped]

The tracks marked [shipped] come from the Plants vs. Zombies 2 soundtrack rip,
stripped of their cover art and re-encoded to Ogg Vorbis. The soundtrack has no
sound effects in it, so the rest of the effect list is still waiting for files.
