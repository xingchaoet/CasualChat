package com.gjg.casualchat.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.gjg.casualchat.R;
import com.gjg.casualchat.controller.adapter.InviteAdapter;
import com.gjg.casualchat.model.Model;
import com.gjg.casualchat.model.bean.InvitationInfo;
import com.gjg.casualchat.utils.Constant;
import com.gjg.casualchat.utils.Tools;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

public class InviteActivity extends AppCompatActivity {
    private ListView lv_invite;
    private InviteAdapter inviteAdapter;
    private LocalBroadcastManager mLBM;
    private InviteAdapter.OnInviteListener mOnInviteListener = new InviteAdapter.OnInviteListener() {
        @Override
        public void onAccept(final InvitationInfo invitationInfo) {
            // 通知环信服务器，点击了接受按钮
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().contactManager().acceptInvitation(invitationInfo.getUser().getHxid());

                        // 数据库更新
                        Model.getInstance().getDbManager().getInviteTableDao().updateInvitationStatus(InvitationInfo.InvitationStatus.INVITE_ACCEPT, invitationInfo.getUser().getHxid());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 页面发生变化
                                Tools.showToastMessage(InviteActivity.this, "接受了邀请");
                                // 刷新页面
                                refresh();
                            }
                        });

                    } catch (HyphenateException e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "接受邀请失败");
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onReject(final InvitationInfo invitationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().contactManager().declineInvitation(invitationInfo.getUser().getHxid());

                        // 数据库变化
                        Model.getInstance().getDbManager().getInviteTableDao().removeInvitation(invitationInfo.getUser().getHxid());

                        // 页面变化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "拒绝成功了");

                                // 刷新页面
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "拒绝失败了");
                            }
                        });
                    }
                }
            });
        }

        // 接受邀请按钮
        @Override
        public void onInviteAccept(final InvitationInfo invitationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 告诉环信服务器接受了邀请
                        EMClient.getInstance().groupManager().acceptInvitation(invitationInfo.getGroup().getGroupId(), invitationInfo.getGroup().getinvitePerson());

                        // 本地数据更新
                        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_ACCEPT_INVITE);
                        Model.getInstance().getDbManager().getInviteTableDao().addInvitation(invitationInfo);

                        // 内存数据的变化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "接受邀请");

                                // 刷新页面
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "接受邀请失败");
                            }
                        });
                    }
                }
            });
        }

        // 拒绝邀请按钮
        @Override
        public void onInviteReject(final InvitationInfo invitationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 告诉环信服务器拒绝了邀请
                        EMClient.getInstance().groupManager().declineInvitation(invitationInfo.getGroup().getGroupId(), invitationInfo.getGroup().getinvitePerson(),"拒绝邀请");

                        // 更新本地数据库
                        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_REJECT_INVITE);
                        Model.getInstance().getDbManager().getInviteTableDao().addInvitation(invitationInfo);

                        // 更新内存的数据
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "拒绝邀请");

                                // 刷新页面
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "拒绝邀请失败");
                            }
                        });

                    }
                }
            });
        }

        // 接受申请按钮
        @Override
        public void onApplicationAccept(final InvitationInfo invitationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 告诉环信服务器接受了申请
                        EMClient.getInstance().groupManager().acceptApplication(invitationInfo.getGroup().getGroupId(), invitationInfo.getGroup().getinvitePerson());

                        // 更新数据库
                        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_ACCEPT_APPLICATION);
                        Model.getInstance().getDbManager().getInviteTableDao().addInvitation(invitationInfo);

                        // 更新内存
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "接受申请");

                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "接受申请失败");
                            }
                        });

                    }
                }
            });
        }

        // 拒绝申请按钮
        @Override
        public void onApplicationReject(final InvitationInfo invitationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 告诉环信服务器拒绝了申请
                        EMClient.getInstance().groupManager().declineApplication(invitationInfo.getGroup().getGroupId(),invitationInfo.getGroup().getinvitePerson(),"拒绝申请");

                        // 更新本地数据库
                        invitationInfo.setStatus(InvitationInfo.InvitationStatus.GROUP_REJECT_APPLICATION);
                        Model.getInstance().getDbManager().getInviteTableDao().addInvitation(invitationInfo);

                        // 更新内存
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "拒绝申请");

                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToastMessage(InviteActivity.this, "拒绝申请失败");
                            }
                        });
                    }
                }
            });
        }
    };

    private BroadcastReceiver inviteChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 刷新页面
            refresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        initView();
        initData();
    }

    private void initData() {
        //初始化数据

        inviteAdapter = new InviteAdapter(this, mOnInviteListener);
        lv_invite.setAdapter(inviteAdapter);

        // 刷新页面
        refresh();

        // 注册邀请信息变化的广播
        mLBM = LocalBroadcastManager.getInstance(this);
        mLBM.registerReceiver(inviteChangedReceiver, new IntentFilter(Constant.CONTACT_INVITE_CHANGED));
        mLBM.registerReceiver(inviteChangedReceiver, new IntentFilter(Constant.GROUP_INVITE_CHANGED));
    }

    private void refresh() {
        // 获取数据库中的所有邀请信息
        List<InvitationInfo> invitations = Model.getInstance().getDbManager().getInviteTableDao().getInvitations();

        // 刷新适配器
        inviteAdapter.refresh(invitations);
    }

    private void initView() {
        lv_invite = (ListView) findViewById(R.id.lv_invite);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLBM.unregisterReceiver(inviteChangedReceiver);
    }
}
