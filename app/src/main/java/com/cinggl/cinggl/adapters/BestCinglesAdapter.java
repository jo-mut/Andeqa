package com.cinggl.cinggl.adapters;

/**
 * Created by J.EL on 7/19/2017.
 */

//CURRENTLY NOT IN USE

public class BestCinglesAdapter {
//    private Context mContext;
//    private static final String EXTRA_POST_KEY = "post key";
//    private static final String EXTRA_USER_UID = "uid";
//    private DatabaseReference databaseReference;
//    private DatabaseReference commentReference;
//    private DatabaseReference usersRef;
//    private  DatabaseReference likesRef;
//    private DatabaseReference ifairReference;
//    private DatabaseReference cingleOwnersReference;
//    private DatabaseReference cingleWalletReference;
//    private FirebaseAuth firebaseAuth;
//    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
//    private Query likesQuery;
//    private Query likesQueryCount;
//    private boolean processLikes = false;
//    private static final double DEFAULT_PRICE = 1.5;
//    private static final double GOLDEN_RATIO = 1.618;
//    private static final int MAX_WIDTH = 200;
//    private static final int MAX_HEIGHT = 200;
//
//    private Query mQuery;
//    private static final String TAG = BestCinglesFragment.class.getSimpleName();
//    private List<Cingle> bestCingles = new ArrayList<>();
//
//    public BestCinglesAdapter(Context mContext) {
//        this.mContext = mContext;
//
//    }
//
//    public void setCingles(List<Cingle> bestCingles) {
//        this.bestCingles = bestCingles;
//        notifyDataSetChanged();
//    }
//
//    public void removeAt(int position){
//        bestCingles.remove(bestCingles.get(position));
//    }
//
//
//    public void animate(BestCinglesViewHolder viewHolder){
//        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
//        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
//
//        final Animation a = AnimationUtils.loadAnimation(mContext, R.anim.anticipate_overshoot_interpolator);
//        viewHolder.itemView.setAnimation(a);
//    }
//
//
//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return bestCingles.size();
//    }
//
//
//    @Override
//    public BestCinglesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.best_cingles_list, parent, false );
//
//        return new BestCinglesViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(BestCinglesViewHolder holder, int position) {
//        final Cingle currentCingle = bestCingles.get(position);
//        final String postKey = bestCingles.get(position).getPushId();
//        Log.d("best cingle postkey", postKey);
//        holder.bindBestCingle(currentCingle);
//    }
//
//    //    @Override
////    public void onBindViewHolder(final BestCinglesViewHolder holder, final int position) {
////        final Cingle thisCingle = bestCingles.get(position);
////        final String postKey = bestCingles.get(position).getPushId();
////        Log.d("best cingle postkey", postKey);
////        holder.bindBestCingle(thisCingle);
//////        animate(holder);
////        //CALL THE METHOD TO ANIMATE RECYCLER_VIEW
////        firebaseAuth = FirebaseAuth.getInstance();
////
////        if (position == bestCingles.size() - 1){
////            holder.cingleMomentTextView.setText("The Cingle Of The Moment");
////        }else if (position == bestCingles.size() - 2){
////            holder.cingleMomentTextView.setText("1st Runners Up Cingle Of The Moment");
////        }else if (position == bestCingles.size() - 3){
////            holder.cingleMomentTextView.setText("2nd Runners Up Cingle Of The Moment");
////        }else {
////            holder.cingleMomentRelativeLayout.setVisibility(View.GONE);
////        }
////
////
////        //DATABASE REFERENCE PATH;
////        commentReference = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS);
////        usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
////        likesRef = FirebaseDatabase.getInstance().getReference(Constants.LIKES);
////        likesQuery = likesRef.child(postKey).limitToFirst(5);
////        likesQueryCount = likesRef;
////        databaseReference = FirebaseDatabase.getInstance()
////                .getReference(Constants.POSTS);
////        ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
////        cingleOwnersReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);
////        cingleWalletReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_WALLET);
////
////        usersRef.keepSynced(true);
////        databaseReference.keepSynced(true);
////        likesRef.keepSynced(true);
////        likesQuery.keepSynced(true);
////        commentReference.keepSynced(true);
////        cingleOwnersReference.keepSynced(true);
////        cingleWalletReference.keepSynced(true);
////        ifairReference.keepSynced(true);
////
////
////        //DATABASE REFERENCE TO READ THE UID OF THE USER IN THE CINGLE
////        databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
////            @Override
////            public void onDataChange(DataSnapshot dataSnapshot) {
////                if (dataSnapshot.exists()){
////                    final String uid = (String) dataSnapshot.child("uid").getValue();
////
////                    holder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            Intent intent = new Intent(mContext, LikesActivity.class);
////                            intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
////                            mContext.startActivity(intent);
////                        }
////                    });
////
////                    holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            Intent intent =  new Intent(mContext, CommentsActivity.class);
////                            intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
////                            mContext.startActivity(intent);
////                        }
////                    });
////
////                    holder.cingleImageView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            Intent intent = new Intent(mContext, FullImageViewActivity.class);
////                            intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
////                            mContext.startActivity(intent);
////                        }
////                    });
////
////                    holder.cingleTradeMethodTextView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            Intent intent =  new Intent(mContext, CingleDetailActivity.class);
////                            intent.putExtra(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
////                            mContext.startActivity(intent);
////                        }
////                    });
////
////                    //SHOW CINGLE SETTINGS TO THE CINGLE CREATOR ONLY
////                    holder.cingleSettingsImageView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            Bundle bundle = new Bundle();
////                            bundle.putString(BestCinglesAdapter.EXTRA_POST_KEY, postKey);
////                            FragmentManager fragmenManager = ((AppCompatActivity)mContext)
////                                    .getSupportFragmentManager();
////                            CingleSettingsDialog cingleSettingsDialog = CingleSettingsDialog
////                                    .newInstance("cingle settings");
////                            cingleSettingsDialog.setArguments(bundle);
////                            cingleSettingsDialog.show(fragmenManager, "cingle settings fragment");
////                        }
////                    });
////
////                    cingleOwnersReference.child(postKey).child("owner")
////                            .addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.exists()){
////                                TransactionDetails transactionDetails = dataSnapshot.getValue(TransactionDetails.class);
////                                final String ownerUid = transactionDetails.getUid();
////                                Log.d(ownerUid, "owner uid");
////
////                                if (firebaseAuth.getCurrentUser().getUid().equals(ownerUid)){
////                                    holder.cingleSettingsImageView.setVisibility(View.VISIBLE);
////                                }else {
////                                    holder.cingleSettingsImageView.setVisibility(View.INVISIBLE);
////                                }
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////
////
////                    holder.ownerImageView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
////                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
////                                intent.putExtra(BestCinglesAdapter.EXTRA_USER_UID, uid);
////                                mContext.startActivity(intent);
////
////                            }else {
////                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
////                                intent.putExtra(BestCinglesAdapter.EXTRA_USER_UID, uid);
////                                mContext.startActivity(intent);
////                            }
////                        }
////                    });
////
////                    holder.creatorImageView.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
////                                Intent intent = new Intent(mContext, PersonalProfileActivity.class);
////                                intent.putExtra(BestCinglesAdapter.EXTRA_USER_UID, uid);
////                                mContext.startActivity(intent);
////
////                            }else {
////                                Intent intent = new Intent(mContext, FollowerProfileActivity.class);
////                                intent.putExtra(BestCinglesAdapter.EXTRA_USER_UID, uid);
////                                mContext.startActivity(intent);
////                            }
////                        }
////                    });
////
////                    //SET THE TRADE METHOD TEXT ACCORDING TO THE TRADE METHOD OF THE CINGLE
//                    ifairReference.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            //SET CINGLE TRADE METHOD WHEN THERE ARE ALL TRADE METHODS
//                            if (dataSnapshot.child("Cingle Lacing").hasChild(postKey)){
//                                holder.cingleTradeMethodTextView.setText("@CingleLacing");
//                            }else if (dataSnapshot.child("Cingle Leasing").hasChild(postKey)){
//                                holder.cingleTradeMethodTextView.setText("@CingleLeasing");
//
//                            }else if (dataSnapshot.child("Cingle Selling").hasChild(postKey)){
//                                holder.cingleTradeMethodTextView.setText("@CingleSelling");
//                            }else if ( dataSnapshot.child("Cingle Backing").hasChild(postKey)){
//                                holder.cingleTradeMethodTextView.setText("@CingleBacking");
//                            }else {
//                                holder.cingleTradeMethodTextView.setText("@NotForTrade");
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
////
////                    ifairReference.child("Cingle Selling").child(postKey).addValueEventListener
////                            (new ValueEventListener() {
////                                @Override
////                                public void onDataChange(DataSnapshot dataSnapshot) {
////                                   if (dataSnapshot.exists()){
////                                       CingleSale cingleSale = dataSnapshot.getValue(CingleSale.class);
////                                       DecimalFormat formatter =  new DecimalFormat("0.00000000");
////
////                                       holder.cingleSalePriceTextView.setText("CSC" + " " + formatter.
////                                               format(cingleSale.getSalePrice()));
////                                   }else {
////                                       holder.cingleSalePriceTitleRelativeLayout.setVisibility(View.GONE);
////                                   }
////                                }
////
////                                @Override
////                                public void onCancelled(DatabaseError databaseError) {
////
////                                }
////                            });
////
////
////                    //SET THE CINGULAN CURRENT USERNAME AND PROFILE IMAGE
////                    usersRef.child(uid).addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.exists()){
////                                final Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
////
////                                holder.usernameTextView.setText(cingulan.getUsername());
////                                Picasso.with(mContext)
////                                        .load(cingulan.getProfileImage())
////                                        .resize(MAX_HEIGHT, MAX_WIDTH)
////                                        .onlyScaleDown()
////                                        .centerCrop()
////                                        .placeholder(R.drawable.profle_image_background)
////                                        .networkPolicy(NetworkPolicy.OFFLINE)
////                                        .into(holder.creatorImageView, new Callback() {
////                                            @Override
////                                            public void onSuccess() {
////
////                                            }
////
////                                            @Override
////                                            public void onError() {
////                                                Picasso.with(mContext)
////                                                        .load(cingulan.getProfileImage())
////                                                        .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                        .onlyScaleDown()
////                                                        .centerCrop()
////                                                        .placeholder(R.drawable.profle_image_background)
////                                                        .into(holder.creatorImageView);
////                                            }
////                                        });
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////
////                    //RETRIVE COMMENTS COUNTS
////                    commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.exists()){
////                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
////                                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "commentsCount");
////                                }
////
////                                holder.commentsCountTextView.setText(dataSnapshot.getChildrenCount() + "");
////                            }
////
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////
////                    //SET THE OWNER OF THE CINGLE
////                    cingleOwnersReference.child(postKey).child("owner").addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.exists()){
////                                TransactionDetails transactionDetails = dataSnapshot.getValue(TransactionDetails.class);
////                                final String ownerUid = transactionDetails.getUid();
////                                usersRef.child(ownerUid).addValueEventListener(new ValueEventListener() {
////                                    @Override
////                                    public void onDataChange(DataSnapshot dataSnapshot) {
////                                        if (dataSnapshot.exists()){
////                                            Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
////                                            final String username = cingulan.getUsername();
////                                            final String profileImage = cingulan.getProfileImage();
////                                            holder.cingleOwnerTextView.setText(username);
////                                            Picasso.with(mContext)
////                                                    .load(profileImage)
////                                                    .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                    .onlyScaleDown()
////                                                    .centerCrop()
////                                                    .placeholder(R.drawable.profle_image_background)
////                                                    .networkPolicy(NetworkPolicy.OFFLINE)
////                                                    .into(holder.ownerImageView, new Callback() {
////                                                        @Override
////                                                        public void onSuccess() {
////
////                                                        }
////
////                                                        @Override
////                                                        public void onError() {
////                                                            Picasso.with(mContext)
////                                                                    .load(profileImage)
////                                                                    .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                                    .onlyScaleDown()
////                                                                    .centerCrop()
////                                                                    .placeholder(R.drawable.profle_image_background)
////                                                                    .into(holder.ownerImageView);
////                                                        }
////                                                    });
////                                        }
////                                    }
////
////                                    @Override
////                                    public void onCancelled(DatabaseError databaseError) {
////
////                                    }
////                                });
////                            }else {
////                                //RETRIEVE THE CREATOR PERSONAL PROFILE DETAIL FOR THE CINGLE
////                                databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
////                                    @Override
////                                    public void onDataChange(DataSnapshot dataSnapshot) {
////                                       if (dataSnapshot.exists()){
////                                           final String creatorUid = dataSnapshot.child("uid").getValue(String.class);
////                                           usersRef.child(creatorUid).addValueEventListener
////                                                   (new ValueEventListener() {
////                                                       @Override
////                                                       public void onDataChange(DataSnapshot dataSnapshot) {
////                                                           Cingulan cingulan = dataSnapshot.getValue(Cingulan.class);
////                                                           final String username = cingulan.getUsername();
////                                                           final String profileImage = cingulan.getProfileImage();
////                                                           holder.cingleOwnerTextView.setText(username);
////                                                           Picasso.with(mContext)
////                                                                   .load(profileImage)
////                                                                   .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                                   .onlyScaleDown()
////                                                                   .centerCrop()
////                                                                   .placeholder(R.drawable.profle_image_background)
////                                                                   .networkPolicy(NetworkPolicy.OFFLINE)
////                                                                   .into(holder.ownerImageView, new Callback() {
////                                                                       @Override
////                                                                       public void onSuccess() {
////
////                                                                       }
////
////                                                                       @Override
////                                                                       public void onError() {
////                                                                           Picasso.with(mContext)
////                                                                                   .load(profileImage)
////                                                                                   .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                                                   .onlyScaleDown()
////                                                                                   .centerCrop()
////                                                                                   .placeholder(R.drawable.profle_image_background)
////                                                                                   .into(holder.ownerImageView);
////                                                                       }
////                                                                   });
////
////                                                       }
////
////                                                       @Override
////                                                       public void onCancelled(DatabaseError databaseError) {
////
////                                                       }
////                                                   });
////
////                                       }
////                                    }
////
////                                    @Override
////                                    public void onCancelled(DatabaseError databaseError) {
////
////                                    }
////                                });
////
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
//////
////                    //RETRIEVE LIKES COUNT
////                    likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            holder.likesCountTextView.setText(dataSnapshot.getChildrenCount() +" " + "Likes");
////
////                            if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())){
////                                holder.likesImageView.setColorFilter(Color.RED);
////                            }else {
////                                holder.likesImageView.setColorFilter(Color.BLACK);
////                            }
////
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////
//////
////                    //RETRIEVE THE FIRST FIVE USERS WHO LIKED
////                    likesRef.child(postKey).addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.exists()){
////                                if (dataSnapshot.getChildrenCount()>0){
////                                    holder.likesRecyclerView.setVisibility(View.VISIBLE);
////                                    //SETUP USERS WHO LIKED THE CINGLE
////                                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Like, WhoLikedViewHolder>
////                                            (Like.class, R.layout.users_who_liked_count, WhoLikedViewHolder.class, likesQuery) {
////                                        @Override
////                                        public int getItemCount() {
////                                            return super.getItemCount();
////
////                                        }
////
////                                        @Override
////                                        public long getItemId(int position) {
////                                            return super.getItemId(position);
////                                        }
////
////                                        @Override
////                                        protected void populateViewHolder(final WhoLikedViewHolder viewHolder, final Like model, final int position) {
////                                            DatabaseReference userRef = getRef(position);
////                                            final String likesPostKey = userRef.getKey();
////                                            Log.d(TAG, "likes post key" + likesPostKey);
////
////                                            likesRef.child(postKey).child(likesPostKey).addValueEventListener(new ValueEventListener() {
////                                                @Override
////                                                public void onDataChange(DataSnapshot dataSnapshot) {
////                                                    if (dataSnapshot.child("uid").exists()){
////                                                        Log.d(TAG, "uid in likes post" + uid);
////                                                        final String uid = (String) dataSnapshot.child("uid").getValue();
////
////                                                        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
////                                                            @Override
////                                                            public void onDataChange(DataSnapshot dataSnapshot) {
////                                                                final String profileImage = (String) dataSnapshot.child("profileImage").getValue();
////
////                                                                Picasso.with(mContext)
////                                                                        .load(profileImage)
////                                                                        .centerCrop()
////                                                                        .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                                        .onlyScaleDown()
////                                                                        .placeholder(R.drawable.profle_image_background)
////                                                                        .networkPolicy(NetworkPolicy.OFFLINE)
////                                                                        .into(viewHolder.whoLikedImageView, new Callback() {
////                                                                            @Override
////                                                                            public void onSuccess() {
////
////                                                                            }
////
////                                                                            @Override
////                                                                            public void onError() {
////                                                                                Picasso.with(mContext)
////                                                                                        .load(profileImage)
////                                                                                        .resize(MAX_HEIGHT, MAX_WIDTH)
////                                                                                        .centerCrop()
////                                                                                        .onlyScaleDown()
////                                                                                        .placeholder(R.drawable.profle_image_background)
////                                                                                        .into(viewHolder.whoLikedImageView);
////
////
////                                                                            }
////                                                                        });
////                                                            }
////
////                                                            @Override
////                                                            public void onCancelled(DatabaseError databaseError) {
////
////                                                            }
////                                                        });
////
////                                                    }
////                                                }
////
////                                                @Override
////                                                public void onCancelled(DatabaseError databaseError) {
////
////                                                }
////                                            });
////
////                                        }
////                                    };
////
////                                    holder.likesRecyclerView.setAdapter(firebaseRecyclerAdapter);
////                                    holder.likesRecyclerView.setHasFixedSize(false);
////                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true);
////                                    layoutManager.setAutoMeasureEnabled(true);
////                                    holder.likesRecyclerView.setNestedScrollingEnabled(false);
////                                    holder.likesRecyclerView.setLayoutManager(layoutManager);
////
////                                }else {
////                                    holder.likesRecyclerView.setVisibility(View.GONE);
////                                }
////
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////
////                    databaseReference.addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.child(postKey).exists()){
////                                holder.likesImageView.setOnClickListener(new View.OnClickListener() {
////                                    @Override
////                                    public void onClick(View view) {
////                                        processLikes = true;
////                                        likesRef.addValueEventListener(new ValueEventListener() {
////                                            @Override
////                                            public void onDataChange(final DataSnapshot dataSnapshot) {
////                                                if(processLikes){
////                                                    if(dataSnapshot.child(postKey).hasChild(firebaseAuth.getCurrentUser().getUid())){
////                                                        likesRef.child(postKey).child(firebaseAuth.getCurrentUser()
////                                                                .getUid())
////                                                                .removeValue();
////                                                        onLikeCounter(false);
////                                                        processLikes = false;
////                                                        holder.likesImageView.setColorFilter(Color.BLACK);
////
////                                                    }else {
////                                                        likesRef.child(postKey).child(firebaseAuth.getCurrentUser().getUid())
////                                                                .child("uid").setValue(firebaseAuth.getCurrentUser().getUid());
////                                                        processLikes = false;
////                                                        onLikeCounter(false);
////                                                        holder.likesImageView.setColorFilter(Color.RED);
////                                                    }
////                                                }
////
////                                                String likesCount = dataSnapshot.child(postKey).getChildrenCount() + "";
////                                                Log.d(likesCount, "all the likes in one cingle");
////                                                //convert children count which is a string to integer
////                                                final int x = Integer.parseInt(likesCount);
////
////                                                if (x > 0){
////                                                    //mille is a thousand likes
////                                                    double MILLE = 1000.0;
////                                                    //get the number of likes per a thousand likes
////                                                    double likesPerMille = x/MILLE;
////                                                    //get the default rate of likes per unit time in seconds;
////                                                    double rateOfLike = 1000.0/1800.0;
////                                                    //get the current rate of likes per unit time in seconds;
////                                                    double currentRateOfLkes = x * rateOfLike/MILLE;
////                                                    //get the current price of cingle
////                                                    final double currentPrice = currentRateOfLkes * DEFAULT_PRICE/rateOfLike;
////                                                    //get the perfection value of cingle's interactivity online
////                                                    double perfectionValue = GOLDEN_RATIO/x;
////                                                    //get the new worth of Cingle price in Sen
////                                                    final double cingleWorth = perfectionValue * likesPerMille * currentPrice;
////                                                    //round of the worth of the cingle to 10 decimal number
////                                                    final double finalPoints = round( cingleWorth, 10);
////
////                                                    Log.d("final points", finalPoints + "");
////
////                                                    cingleWalletReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
////                                                        @Override
////                                                        public void onDataChange(DataSnapshot dataSnapshot) {
////                                                            if (dataSnapshot.exists()) {
////                                                                final Balance balance = dataSnapshot.getValue(Balance.class);
////                                                                final double amountRedeemed = balance.getAmountRedeemed();
////                                                                Log.d(amountRedeemed + "", "amount redeemed");
////                                                                final  double amountDeposited = balance.getAmountDeposited();
////                                                                Log.d(amountDeposited + "", "amount deposited");
////                                                                final double senseCredits = amountDeposited + finalPoints;
////                                                                Log.d("sense credits", senseCredits + "");
////                                                                final double totalSenseCredits = senseCredits - amountRedeemed;
////                                                                Log.d("total sense credits", totalSenseCredits + "");
////                                                                databaseReference.child(postKey).child("sensepoint").setValue(totalSenseCredits);
////                                                            }else {
////                                                                databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
////                                                            }
////                                                        }
////
////                                                        @Override
////                                                        public void onCancelled(DatabaseError databaseError) {
////
////                                                        }
////                                                    });
////                                                }
////                                                else{
////                                                    final double finalPoints = 0.00;
////                                                    Log.d("final points", finalPoints + "");
////                                                    cingleWalletReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
////                                                        @Override
////                                                        public void onDataChange(DataSnapshot dataSnapshot) {
////                                                            if (dataSnapshot.exists()) {
////                                                                final Balance balance = dataSnapshot.getValue(Balance.class);
////                                                                final double amountRedeemed = balance.getAmountRedeemed();
////                                                                Log.d(amountRedeemed + "", "amount redeemed");
////                                                                final  double amountDeposited = balance.getAmountDeposited();
////                                                                Log.d(amountDeposited + "", "amount deposited");
////                                                                final double senseCredits = amountDeposited + finalPoints;
////                                                                Log.d("sense credits", senseCredits + "");
////                                                                final double totalSenseCredits = senseCredits - amountRedeemed;
////                                                                Log.d("total sense credits", totalSenseCredits + "");
////                                                                databaseReference.child(postKey).child("sensepoint").setValue(totalSenseCredits);
////                                                            }else {
////                                                                databaseReference.child(postKey).child("sensepoint").setValue(finalPoints);
////                                                            }
////                                                        }
////
////                                                        @Override
////                                                        public void onCancelled(DatabaseError databaseError) {
////
////                                                        }
////                                                    });
////
////                                                }
////
////                                            }
////
////                                            @Override
////                                            public void onCancelled(DatabaseError databaseError) {
////
////                                            }
////                                        });
////                                    }
////                                });
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////                }
////            }
////
////            @Override
////            public void onCancelled(DatabaseError databaseError) {
////
////            }
////        });
////
////    }
//
//    private void onLikeCounter(final boolean increament){
//        likesRef.runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                if(mutableData.getValue() != null){
//                    int value = mutableData.getValue(Integer.class);
//                    if(increament){
//                        value++;
//                    }else{
//                        value--;
//                    }
//                    mutableData.setValue(value);
//                }
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean b,
//                                   DataSnapshot dataSnapshot) {
//                Log.d(TAG, "likeTransaction:onComplete" + databaseError);
//
//            }
//        });
//    }
//
//    //region listeners
//    private static double round(double value, int places) {
//        if (places < 0) throw new IllegalArgumentException();
//
//        BigDecimal bd = new BigDecimal(value);
//        bd = bd.setScale(places, RoundingMode.HALF_UP);
//        return bd.doubleValue();
//    }
//
//
//    public void clearAll(){
//        bestCingles.clear();
//        notifyDataSetChanged();
//    }

}
